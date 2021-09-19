/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Message;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Vector;

import de.blinkt.openvpn.R;

public class VpnStatus {


    final static java.lang.Object READ_FILE_LOCK = new Object();
    static final int MAXLOGENTRIES = 1000;
    static final byte[] OFFICALKEY = {-58, -42, -44, -106, 90, -88, -87, -88, -52, -124, 84, 117, 66, 79, -112, -111, -46, 86, -37, 109};
    static final byte[] OFFICALDEBUGKEY = {-99, -69, 45, 71, 114, -116, 82, 66, -99, -122, 50, -70, -56, -111, 98, -35, -65, 105, 82, 43};
    static final byte[] AMAZONKEY = {-116, -115, -118, -89, -116, -112, 120, 55, 79, -8, -119, -23, 106, -114, -85, -56, -4, 105, 26, -57};
    static final byte[] FDROIDKEY = {-92, 111, -42, -46, 123, -96, -60, 79, -27, -31, 49, 103, 11, -54, -68, -27, 17, 2, 121, 104};
    private static final Vector<StateListener> STATE_LISTENER;
    public static TrafficHistory trafficHistory;
    static boolean readFileLog = false;
    private static String mLaststatemsg = "";
    private static String mLaststate = "NOPROCESS";
    private static int mLastStateresid = R.string.state_noprocess;
    private static Intent mLastIntent = null;
    private static HandlerThread mHandlerThread;
    private static String mLastConnectedVPNUUID;
    private static ConnectionStatus mLastLevel = ConnectionStatus.LEVEL_NOTCONNECTED;

    static {

        STATE_LISTENER = new Vector<>();
        trafficHistory = new TrafficHistory();

        logInformation();

    }




    public static boolean isVPNActive() {
        return mLastLevel != ConnectionStatus.LEVEL_AUTH_FAILED && !(mLastLevel == ConnectionStatus.LEVEL_NOTCONNECTED);
    }

    public static String getLastCleanLogMessage(Context c) {
        String message;
        if (mLastLevel == ConnectionStatus.LEVEL_CONNECTED) {
            String[] parts = mLaststatemsg.split(",");
            message = parts[1];
            return message;
        } else {
            return "";
        }

    }


    public static void setConnectedVPNProfile(String uuid) {
        mLastConnectedVPNUUID = uuid;
        for (StateListener sl : STATE_LISTENER) {
            sl.setConnectedVPN(uuid);
        }
    }

    public static String getLastConnectedVPNProfile() {
        return mLastConnectedVPNUUID;
    }

    public static void setTrafficHistory(TrafficHistory trafficHistory) {
        VpnStatus.trafficHistory = trafficHistory;
    }



    private static void logInformation() {
        String nativeAPI;
        try {
            nativeAPI = NativeUtils.getNativeAPI();
        } catch (UnsatisfiedLinkError ignore) {
            nativeAPI = "error";
        }


    }



    public synchronized static void addStateListener(StateListener sl) {
        if (!STATE_LISTENER.contains(sl)) {
            STATE_LISTENER.add(sl);
            if (mLaststate != null) {
                sl.updateState(mLaststate, mLaststatemsg, mLastStateresid, mLastLevel, mLastIntent);
            }
        }
    }

    private static int getLocalizedState(String state) {
        switch (state) {
            case "CONNECTING":
                return R.string.state_connecting;
            case "WAIT":
                return R.string.state_wait;
            case "AUTH":
                return R.string.state_auth;
            case "GET_CONFIG":
                return R.string.state_get_config;
            case "ASSIGN_IP":
                return R.string.state_assign_ip;
            case "ADD_ROUTES":
                return R.string.state_add_routes;
            case "CONNECTED":
                return R.string.state_connected;
            case "DISCONNECTED":
                return R.string.state_disconnected;
            case "RECONNECTING":
                return R.string.state_reconnecting;
            case "EXITING":
                return R.string.state_exiting;
            case "RESOLVE":
                return R.string.state_resolve;
            case "TCP_CONNECT":
                return R.string.state_tcp_connect;
            case "AUTH_PENDING":
                return R.string.state_auth_pending;
            default:
                return R.string.unknown_state;
        }

    }

    public static void updateStatePause(OpenVPNManagement.pauseReason pauseReason) {
        switch (pauseReason) {
            case noNetwork:
                VpnStatus.updateStateString("NONETWORK", "", R.string.state_nonetwork, ConnectionStatus.LEVEL_NONETWORK);
                break;
            case screenOff:
                VpnStatus.updateStateString("SCREENOFF", "", R.string.state_screenoff, ConnectionStatus.LEVEL_VPNPAUSED);
                break;
            case userPause:
                VpnStatus.updateStateString("USERPAUSE", "", R.string.state_userpause, ConnectionStatus.LEVEL_VPNPAUSED);
                break;
            default:
                break;
        }

    }

    private static ConnectionStatus getLevel(String state) {
        String[] noreplyet = {"CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT"};
        String[] reply = {"AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES", "AUTH_PENDING"};
        String[] connected = {"CONNECTED"};
        String[] notconnected = {"DISCONNECTED", "EXITING"};

        for (String x : noreplyet) {
            if (state.equals(x)) {
                return ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;
            }
        }

        for (String x : reply) {
            if (state.equals(x)) {
                return ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;
            }
        }

        for (String x : connected) {
            if (state.equals(x)) {
                return ConnectionStatus.LEVEL_CONNECTED;
            }
        }

        for (String x : notconnected) {
            if (state.equals(x)) {
                return ConnectionStatus.LEVEL_NOTCONNECTED;
            }
        }

        return ConnectionStatus.UNKNOWN_LEVEL;

    }

    public synchronized static void removeStateListener(StateListener sl) {
        STATE_LISTENER.remove(sl);
    }


    static void updateStateString(String state, String msg) {
        // We want to skip announcing that we are trying to get the configuration since
        // this is just polling until the user input has finished.be
        if (mLastLevel == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT && state.equals("GET_CONFIG")) {
            return;
        }
        int rid = getLocalizedState(state);
        ConnectionStatus level = getLevel(state);
        updateStateString(state, msg, rid, level);
    }

    public synchronized static void updateStateString(String state, String msg, int resid, ConnectionStatus level) {
        updateStateString(state, msg, resid, level, null);
    }

    public synchronized static void updateStateString(String state, String msg, int resid, ConnectionStatus level, Intent intent) {
        // Workound for OpenVPN doing AUTH and wait and being connected
        // Simply ignore these state
        if (mLastLevel == ConnectionStatus.LEVEL_CONNECTED &&
                (state.equals("WAIT") || state.equals("AUTH"))) {
            return;
        }

        mLaststate = state;
        mLaststatemsg = msg;
        mLastStateresid = resid;
        mLastLevel = level;
        mLastIntent = intent;


        for (StateListener sl : STATE_LISTENER) {
            sl.updateState(state, msg, resid, level, intent);
        }
        //newLogItem(new LogItem((LogLevel.DEBUG), String.format("New OpenVPN Status (%s->%s): %s",state,level.toString(),msg)));
    }




    public interface StateListener {
        void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent);

        void setConnectedVPN(String uuid);
    }
}

