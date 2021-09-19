/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Vector;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;

public class OpenVpnManagementThread implements Runnable, OpenVPNManagement {

    public static final int ORBOT_TIMEOUT_MS = 20 * 1000;
    private static final String TAG = "openvpn";
    private static final Vector<OpenVpnManagementThread> ACTIVE = new Vector<>();
    private final Handler mResumeHandler;
    private final VpnProfile mProfile;
    private final OpenVPNService mOpenVPNService;
    private final LinkedList<FileDescriptor> mFDList = new LinkedList<>();
    private LocalSocket mSocket;
    private final Runnable orbotStatusTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            sendProxyCMD(Connection.ProxyType.SOCKS5, "127.0.0.1", Integer.toString(OrbotHelper.SOCKS_PROXY_PORT_DEFAULT), false);
            OrbotHelper.get(mOpenVPNService).removeStatusCallback(statusCallback);

        }
    };
    private final OrbotHelper.StatusCallback statusCallback = new OrbotHelper.StatusCallback() {

        @Override
        public void onStatus(Intent statusIntent) {
            StringBuilder extras = new StringBuilder();
            for (String key : statusIntent.getExtras().keySet()) {
                Object val = statusIntent.getExtras().get(key);

                extras.append(String.format(Locale.ENGLISH, "%s - '%s'", key, val == null ? "null" : val.toString()));
            }
        }

        @Override
        public void onNotYetInstalled() {

        }

        @Override
        public void onOrbotReady(Intent intent, String socksHost, int socksPort) {
            mResumeHandler.removeCallbacks(orbotStatusTimeOutRunnable);
            sendProxyCMD(Connection.ProxyType.SOCKS5, socksHost, Integer.toString(socksPort), false);
            OrbotHelper.get(mOpenVPNService).removeStatusCallback(this);
        }

        @Override
        public void onDisabled(Intent intent) {
        }
    };
    private LocalServerSocket mServerSocket;
    private boolean mWaitingForRelease = false;
    private long mLastHoldRelease = 0;
    private LocalSocket mServerSocketLocal;
    private pauseReason lastPauseReason = pauseReason.noNetwork;
    private PausedStateCallback mPauseCallback;
    private final Runnable mResumeHoldRunnable = () -> {
        if (shouldBeRunning()) {
            releaseHoldCmd();
        }
    };
    private boolean mShuttingDown;
    private transient Connection mCurrentProxyConnection;

    public OpenVpnManagementThread(VpnProfile profile, OpenVPNService openVpnService) {
        mProfile = profile;
        mOpenVPNService = openVpnService;
        mResumeHandler = new Handler(openVpnService.getMainLooper());

    }

    private static boolean stopOpenVPN() {
        synchronized (ACTIVE) {
            boolean sendCMD = false;
            for (OpenVpnManagementThread mt : ACTIVE) {
                sendCMD = mt.managmentCommand("signal SIGINT\n");
                try {
                    if (mt.mSocket != null) {
                        mt.mSocket.close();
                    }
                } catch (IOException e) {
                    // Ignore close error on already closed socket
                }
            }
            return sendCMD;
        }
    }

    public boolean openManagementInterface(@NonNull Context c) {
        // Could take a while to open connection
        int tries = 8;

        String socketName = (c.getCacheDir().getAbsolutePath() + "/" + "mgmtsocket");
        // The mServerSocketLocal is transferred to the LocalServerSocket, ignore warning

        mServerSocketLocal = new LocalSocket();

        while (tries > 0 && !mServerSocketLocal.isBound()) {
            try {
                mServerSocketLocal.bind(new LocalSocketAddress(socketName,
                        LocalSocketAddress.Namespace.FILESYSTEM));
            } catch (IOException e) {
                // wait 300 ms before retrying
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }

            }
            tries--;
        }

        try {

            mServerSocket = new LocalServerSocket(mServerSocketLocal.getFileDescriptor());
            return true;
        } catch (IOException e) {
        }
        return false;


    }

    /**
     * @param cmd command to write to management socket
     * @return true if command have been sent
     */
    public boolean managmentCommand(String cmd) {
        try {
            if (mSocket != null && mSocket.getOutputStream() != null) {
                mSocket.getOutputStream().write(cmd.getBytes());
                mSocket.getOutputStream().flush();
                return true;
            }
        } catch (IOException e) {
            // Ignore socket stack traces
        }
        return false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        //	mSocket.setSoTimeout(5); // Setting a timeout cannot be that bad

        String pendingInput = "";
        synchronized (ACTIVE) {
            ACTIVE.add(this);
        }

        try {
            // Wait for a client to connect
            mSocket = mServerSocket.accept();
            InputStream instream = mSocket.getInputStream();


            // Close the management socket after client connected
            try {
                mServerSocket.close();
            } catch (IOException e) {
            }

            // Closing one of the two sockets also closes the other
            //mServerSocketLocal.close();
            managmentCommand("version 3\n");

            while (true) {

                int numbytesread = instream.read(buffer);
                if (numbytesread == -1) {
                    return;
                }

                FileDescriptor[] fds = null;
                try {
                    fds = mSocket.getAncillaryFileDescriptors();
                } catch (IOException e) {
                }
                if (fds != null) {
                    Collections.addAll(mFDList, fds);
                }

                @SuppressLint({"NewApi", "LocalSuppress"}) String input = new String(buffer, 0, numbytesread, StandardCharsets.UTF_8);

                pendingInput += input;

                pendingInput = processInput(pendingInput);


            }
        } catch (IOException e) {
            if (!e.getMessage().equals("socket closed") && !e.getMessage().equals("Connection reset by peer")) {
            }
        }
        synchronized (ACTIVE) {
            ACTIVE.remove(this);
        }
    }

    //! Hack O Rama 2000!
    private void protectFileDescriptor(FileDescriptor fd) {
        try {
            Method getInt = FileDescriptor.class.getDeclaredMethod("getInt$");
            int fdint = (Integer) getInt.invoke(fd);

            // You can even get more evil by parsing toString() and extract the int from that :)

            boolean result = mOpenVPNService.protect(fdint);


            //ParcelFileDescriptor pfd = ParcelFileDescriptor.fromFd(fdint);
            //pfd.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fdCloseLollipop(fd);
            } else {
                NativeUtils.jniclose(fdint);
            }
            return;
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fdCloseLollipop(FileDescriptor fd) {
        try {
            Os.close(fd);
        } catch (Exception e) {
        }
    }

    private String processInput(String pendingInput) {


        while (pendingInput.contains("\n")) {
            String[] tokens = pendingInput.split("\\r?\\n", 2);
            processCommand(tokens[0]);
            if (tokens.length == 1)
                // No second part, newline was at the end
            {
                pendingInput = "";
            } else {
                pendingInput = tokens[1];
            }
        }
        return pendingInput;
    }

    private void processCommand(String command) {
        //Log.i(TAG, "Line from managment" + command);

        if (command.startsWith(">") && command.contains(":")) {
            String[] parts = command.split(":", 2);
            String cmd = parts[0].substring(1);
            String argument = parts[1];

            switch (cmd) {
                case "INFO":
                    /* Ignore greeting from management */
                    return;
                case "PASSWORD":
                    processPWCommand(argument);
                    break;
                case "HOLD":
                    handleHold(argument);
                    break;
                case "NEED-OK":
                    processNeedCommand(argument);
                    break;
                case "BYTECOUNT":
                    break;
                case "STATE":
                    if (!mShuttingDown) {
                        processState(argument);
                    }
                    break;
                case "PROXY":
                    processProxyCMD(argument);
                    break;
                case "LOG":
                    break;
                case "PK_SIGN":
                    processSignCommand(argument);
                    break;
                case "INFOMSG":
                    break;
                default:

                    break;
            }
        } else if (command.startsWith("SUCCESS:")) {
            /* Ignore this kind of message too */
            return;
        } else if (command.startsWith("PROTECTFD: ")) {
            FileDescriptor fdtoprotect = mFDList.pollFirst();
            if (fdtoprotect != null) {
                protectFileDescriptor(fdtoprotect);
            }
        } else {
        }
    }



    boolean shouldBeRunning() {
        if (mPauseCallback == null) {
            return false;
        } else {
            return mPauseCallback.shouldBeRunning();
        }
    }

    private void handleHold(String argument) {
        mWaitingForRelease = true;
        int waittime = Integer.parseInt(argument.split(":")[1]);
        if (shouldBeRunning()) {
            if (waittime > 1) {
                VpnStatus.updateStateString("CONNECTRETRY", String.valueOf(waittime),
                        R.string.state_waitconnectretry, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);
            }
            mResumeHandler.postDelayed(mResumeHoldRunnable, waittime * 1000);

        } else {
            VpnStatus.updateStatePause(lastPauseReason);
        }
    }

    private void releaseHoldCmd() {
        mResumeHandler.removeCallbacks(mResumeHoldRunnable);
        if ((System.currentTimeMillis() - mLastHoldRelease) < 5000) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }

        }
        mWaitingForRelease = false;
        mLastHoldRelease = System.currentTimeMillis();
        managmentCommand("hold release\n");
        managmentCommand("bytecount " + mBytecountInterval + "\n");
        managmentCommand("state on\n");
        //managmentCommand("log on all\n");
    }

    public void releaseHold() {
        if (mWaitingForRelease) {
            releaseHoldCmd();
        }
    }

    private void processProxyCMD(String argument) {
        String[] args = argument.split(",", 3);

        Connection.ProxyType proxyType = Connection.ProxyType.NONE;

        int connectionEntryNumber = Integer.parseInt(args[0]) - 1;
        String proxyport = null;
        String proxyname = null;
        boolean proxyUseAuth = false;

        if (mProfile.mConnections.length > connectionEntryNumber) {
            Connection connection = mProfile.mConnections[connectionEntryNumber];
            proxyType = connection.mProxyType;
            proxyname = connection.mProxyName;
            proxyport = connection.mProxyPort;
            proxyUseAuth = connection.mUseProxyAuth;

            // Use transient variable to remember http user/password
            mCurrentProxyConnection = connection;

        }

        // atuo detection of proxy
        if (proxyType == Connection.ProxyType.NONE) {
            SocketAddress proxyaddr = ProxyDetection.detectProxy(mProfile);
            if (proxyaddr instanceof InetSocketAddress) {
                InetSocketAddress isa = (InetSocketAddress) proxyaddr;
                proxyType = Connection.ProxyType.HTTP;
                proxyname = isa.getHostName();
                proxyport = String.valueOf(isa.getPort());
                proxyUseAuth = false;

            }
        }


        if (args.length >= 2 && proxyType == Connection.ProxyType.HTTP) {
            String proto = args[1];
            if (proto.equals("UDP")) {
                proxyname = null;
            }
        }


        if (proxyType == Connection.ProxyType.ORBOT) {
            VpnStatus.updateStateString("WAIT_ORBOT", "Waiting for Orbot to start", R.string.state_waitorbot, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);
            OrbotHelper orbotHelper = OrbotHelper.get(mOpenVPNService);

            mResumeHandler.postDelayed(orbotStatusTimeOutRunnable, ORBOT_TIMEOUT_MS);
            orbotHelper.addStatusCallback(mOpenVPNService, statusCallback);

            orbotHelper.sendOrbotStartAndStatusBroadcast();

        } else {
            sendProxyCMD(proxyType, proxyname, proxyport, proxyUseAuth);
        }
    }

    private void sendProxyCMD(Connection.ProxyType proxyType, String proxyname, String proxyport, boolean usePwAuth) {
        if (proxyType != Connection.ProxyType.NONE && proxyname != null) {


            String pwstr = usePwAuth ? " auto" : "";

            String proxycmd = String.format(Locale.ENGLISH, "proxy %s %s %s%s\n",
                    proxyType == Connection.ProxyType.HTTP ? "HTTP" : "SOCKS",
                    proxyname, proxyport, pwstr);
            managmentCommand(proxycmd);
        } else {
            managmentCommand("proxy NONE\n");
        }
    }

    private void processState(String argument) {
        String[] args = argument.split(",", 3);
        String currentstate = args[1];

        if (args[2].equals(",,")) {
            VpnStatus.updateStateString(currentstate, "");
        } else {
            VpnStatus.updateStateString(currentstate, args[2]);
        }
    }


    private void processNeedCommand(String argument) {
        int p1 = argument.indexOf('\'');
        int p2 = argument.indexOf('\'', p1 + 1);

        String needed = argument.substring(p1 + 1, p2);
        String extra = argument.split(":", 2)[1];

        String status = "ok";


        switch (needed) {
            case "PROTECTFD":
                FileDescriptor fdtoprotect = mFDList.pollFirst();
                protectFileDescriptor(fdtoprotect);
                break;
            case "DNSSERVER":
            case "DNS6SERVER":
                mOpenVPNService.addDNS(extra);
                break;
            case "DNSDOMAIN":
                mOpenVPNService.setDomain(extra);
                break;
            case "ROUTE": {
                String[] routeparts = extra.split(" ");

            /*
            buf_printf (&out, "%s %s %s dev %s", network, netmask, gateway, rgi->iface);
            else
            buf_printf (&out, "%s %s %s", network, netmask, gateway);
            */

                if (routeparts.length == 5) {
                    //if (BuildConfig.DEBUG)
                    //                assertEquals("dev", routeparts[3]);
                    mOpenVPNService.addRoute(routeparts[0], routeparts[1], routeparts[2], routeparts[4]);
                } else if (routeparts.length >= 3) {
                    mOpenVPNService.addRoute(routeparts[0], routeparts[1], routeparts[2], null);
                }

                break;
            }
            case "ROUTE6": {
                String[] routeparts = extra.split(" ");
                mOpenVPNService.addRoutev6(routeparts[0], routeparts[1]);
                break;
            }
            case "IFCONFIG":
                String[] ifconfigparts = extra.split(" ");
                int mtu = Integer.parseInt(ifconfigparts[2]);
                mOpenVPNService.setLocalIP(ifconfigparts[0], ifconfigparts[1], mtu, ifconfigparts[3]);
                break;
            case "IFCONFIG6":
                String[] ifconfig6parts = extra.split(" ");
                mtu = Integer.parseInt(ifconfig6parts[1]);
                mOpenVPNService.setMtu(mtu);
                mOpenVPNService.setLocalIPv6(ifconfig6parts[0]);
                break;
            case "PERSIST_TUN_ACTION":
                // check if tun cfg stayed the same
                status = mOpenVPNService.getTunReopenStatus();
                break;
            case "OPENTUN":
                if (sendTunFD(needed, extra)) {
                    return;
                } else {
                    status = "cancel";
                }
                // This not nice or anything but setFileDescriptors accepts only FilDescriptor class :(

                break;
            default:
                Log.e(TAG, "Unknown needok command " + argument);
                return;
        }

        String cmd = String.format("needok '%s' %s\n", needed, status);
        managmentCommand(cmd);
    }

    private boolean sendTunFD(String needed, String extra) {
        if (!extra.equals("tun")) {
            // We only support tun

            return false;
        }
        ParcelFileDescriptor pfd = mOpenVPNService.openTun();
        if (pfd == null) {
            return false;
        }

        Method setInt;
        int fdint = pfd.getFd();
        try {
            setInt = FileDescriptor.class.getDeclaredMethod("setInt$", int.class);
            FileDescriptor fdtosend = new FileDescriptor();

            setInt.invoke(fdtosend, fdint);

            FileDescriptor[] fds = {fdtosend};
            mSocket.setFileDescriptorsForSend(fds);

            // Trigger a send so we can close the fd on our side of the channel
            // The API documentation fails to mention that it will not reset the file descriptor to
            // be send and will happily send the file descriptor on every write ...
            String cmd = String.format("needok '%s' %s\n", needed, "ok");
            managmentCommand(cmd);

            // Set the FileDescriptor to null to stop this mad behavior
            mSocket.setFileDescriptorsForSend(null);

            pfd.close();

            return true;
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException |
                IOException | IllegalAccessException exp) {
        }

        return false;
    }

    private void processPWCommand(String argument) {
        //argument has the form 	Need 'Private Key' password
        // or  ">PASSWORD:Verification Failed: '%s' ['%s']"
        String needed;


        try {
            // Ignore Auth token message, already managed by openvpn itself
            if (argument.startsWith("Auth-Token:")) {
                return;
            }

            int p1 = argument.indexOf('\'');
            int p2 = argument.indexOf('\'', p1 + 1);
            needed = argument.substring(p1 + 1, p2);
            if (argument.startsWith("Verification Failed")) {
                proccessPWFailed(needed, argument.substring(p2 + 1));
                return;
            }
        } catch (StringIndexOutOfBoundsException sioob) {
            return;
        }

        String pw = null;
        String username = null;

        if (needed.equals("Private Key")) {
            pw = mProfile.getPasswordPrivateKey();
        } else if (needed.equals("Auth")) {
            pw = mProfile.getPasswordAuth();
            username = mProfile.mUsername;

        } else if (needed.equals("HTTP Proxy")) {
            if (mCurrentProxyConnection != null) {
                pw = mCurrentProxyConnection.mProxyAuthPassword;
                username = mCurrentProxyConnection.mProxyAuthUser;
            }
        }
        if (pw != null) {
            if (username != null) {
                String usercmd = String.format("username '%s' %s\n",
                        needed, VpnProfile.openVpnEscape(username));
                managmentCommand(usercmd);
            }
            String cmd = String.format("password '%s' %s\n", needed, VpnProfile.openVpnEscape(pw));
            managmentCommand(cmd);
        } else {
            mOpenVPNService.requestInputFromUser(R.string.password, needed);
        }

    }

    private void proccessPWFailed(String needed, String args) {
        VpnStatus.updateStateString("AUTH_FAILED", needed + args, R.string.state_auth_failed, ConnectionStatus.LEVEL_AUTH_FAILED);
    }

    @Override
    public void networkChange(boolean samenetwork) {
        if (mWaitingForRelease) {
            releaseHold();
        } else if (samenetwork) {
            managmentCommand("network-change samenetwork\n");
        } else {
            managmentCommand("network-change\n");
        }
    }

    @Override
    public void setPauseCallback(PausedStateCallback callback) {
        mPauseCallback = callback;
    }

    @Override
    public void sendCRResponse(String response) {
        managmentCommand("cr-response " + response + "\n");
    }

    public void signalusr1() {
        mResumeHandler.removeCallbacks(mResumeHoldRunnable);
        if (!mWaitingForRelease) {
            managmentCommand("signal SIGUSR1\n");
        } else
            // If signalusr1 is called update the state string
            // if there is another for stopping
        {
            VpnStatus.updateStatePause(lastPauseReason);
        }
    }

    @Override
    public void reconnect() {
        signalusr1();
        releaseHold();
    }

    private void processSignCommand(String argument) {

        String[] arguments = argument.split(",");

        boolean pkcs1padding = arguments[1].equals("RSA_PKCS1_PADDING");
        String signed_string = mProfile.getSignedData(mOpenVPNService, arguments[0], pkcs1padding);

        if (signed_string == null) {
            managmentCommand("pk-sig\n");
            managmentCommand("\nEND\n");
            stopOpenVPN();
            return;
        }
        managmentCommand("pk-sig\n");
        managmentCommand(signed_string);
        managmentCommand("\nEND\n");
    }

    @Override
    public void pause(pauseReason reason) {
        lastPauseReason = reason;
        signalusr1();
    }

    @Override
    public void resume() {
        releaseHold();
        /* Reset the reason why we are disconnected */
        lastPauseReason = pauseReason.noNetwork;
    }

    @Override
    public boolean stopVPN(boolean replaceConnection) {
        boolean stopSucceed = stopOpenVPN();
        if (stopSucceed) {
            mShuttingDown = true;

        }
        return stopSucceed;
    }

}
