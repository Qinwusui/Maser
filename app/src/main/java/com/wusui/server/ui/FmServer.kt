package com.wusui.server.ui

import android.annotation.SuppressLint
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.PowerManager
import android.os.RemoteException
import android.provider.Settings
import android.view.*
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.*
import com.tapadoo.alerter.Alerter
import com.wusui.server.R
import com.wusui.server.activity.MainActivity
import com.wusui.server.adpters.ServerListAdapter
import com.wusui.server.beans.Colors
import com.wusui.server.beans.Config
import com.wusui.server.beans.Login
import com.wusui.server.databinding.FragmentServerBinding
import com.wusui.server.model.ServerModel
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import solid.ren.skinlibrary.base.SkinBaseFragment


/**
 * @author wusui
 */
class FmServer : SkinBaseFragment() {
    val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            setStatus(intent.getStringExtra("state"))
            serverModel.strextra = intent.getStringExtra("state")
        }
    }
    private val serverModel by viewModels<ServerModel>()
    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        initView()

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .unregisterReceiver(broadcastReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun status(status: String) = if (status == "connecting") {
        Alerter.create(requireActivity())
            .setBackgroundColorInt(Colors.colorSkin)
            .setTitle("正在连接...")
            .show()
    } else {
        null
    }

    private fun confirmDisconnect() {
        val builder = MaterialDialog.Builder(requireContext())
        builder.title("提示")
            .content("是否断开？")
            .onPositive { _: MaterialDialog?, _: DialogAction? -> stopVpn() }
            .positiveText("断开")
            .negativeText("点错了")
            .show()
    }

    private fun preparevpn(c: Config, position: Int) {
        if (Login.qq == null || Login.code == null) {
            Alerter.create(requireActivity())
                .setTitle("你还没有登录，前往登录页点击头像登录（")
                .setBackgroundColorInt(Colors.colorSkin)
                .addButton("登录", R.style.AlerterButton) {
                    binding.root.rootView.findViewById<ViewPager2>(R.id.viewPager)
                        .setCurrentItem(2, true)
                }
                .show()
        } else {
            if (!serverModel.vpnStart) {
                val intent = VpnService.prepare(requireContext())
                if (intent != null) {
                    startActivityForResult(intent, 1)
                }
                startVpn(c, position)
                status("connecting")

            } else {
                if (stopVpn()) {
                    Alerter.create(requireActivity()).setBackgroundColorInt(Colors.colorSkin)
                        .setTitle("已经断开~").show()
                }
            }
        }

    }

    fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            status("connect")
            serverModel.vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun setStatus(connectionState: String?) {
        if (connectionState != null) {
            when (connectionState) {
                "DISCONNECTED" -> {
                    serverModel.vpnStart = false
                    status("connect")
//                    OpenVPNService.setDefaultStatus()
                    Alerter.create(requireActivity())
                        .setBackgroundColorInt(Colors.colorSkin)
                        .setTitle("已经断开~")
                        .show()
                    VibrateUtils.vibrate(100)
                }
                "CONNECTED" -> {
                    VibrateUtils.vibrate(100)

                    serverModel.vpnStart = true
                    Alerter.create(requireActivity())
                        .setBackgroundColorInt(Colors.colorSkin)
                        .setDuration(10000)
                        .addButton("断开", R.style.AlerterButton) { stopVpn() }
                        .addButton("复制IP", R.style.AlerterButton) {
                            ClipboardUtils.copyText(VpnStatus.getLastCleanLogMessage(requireContext()))
                            ToastUtils.showShort("已经复制！")
                        }
                        .setTitle("已连接！当前内网IP为" + VpnStatus.getLastCleanLogMessage(requireContext()))
                        .show()
                    val pm =
                        requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (!pm.isIgnoringBatteryOptimizations(AppUtils.getAppPackageName())) {
                        //判断是否处于电池优化名单
                        MaterialDialog.Builder(requireContext())
                            .title("重要提示")
                            .content(
                                """
                                        Maser目前处于系统电池优化名单
                                        这将可能会导致系统自动回收Maser所开启的服务
                                        是否将Maser列入未优化名单？
                                        """.trimIndent()
                            )
                            .positiveText("是")
                            .positiveColor(ColorUtils.getColor(R.color.shuiyaqing))
                            .onPositive { _, _ ->
                                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))

                            }
                            .negativeText("取消")
                            .negativeColor(ColorUtils.getColor(R.color.hui))
                            .show()
                    }

                    if (RomUtils.isXiaomi() && SPUtils.getInstance()
                            .getBoolean("isShowMiui", false)
                    ) {
                        MaterialDialog.Builder(requireContext())
                            .title("关于MIUI的提示")
                            .content(
                                """
                                       由于MIUI严格的后台管理机制，很多应用在后台时都容易被清理
                                       建议你在联机之前打开最近任务锁定Maser，防止后台清除Maser的服务
                                    """.trimIndent()
                            )
                            .positiveText("好的")
                            .onPositive { _, _ ->
                                launchRecent()
                            }
                            .negativeText("取消")
                            .onAny { _, _ ->
                                SPUtils.getInstance().put("isShowMiui", true)
                            }
                            .show()
                    }
                }
                "RECONNECTING" -> status("connecting")

            }

        }
    }

    fun launchRecent() {
        val componetName = ComponentName(
            "com.miui.home",  //这个是另外一个应用程序的包名
            "com.miui.home.recents.RecentsActivity"
        ) //这个参数是要启动的Activity的全路径名

        val intent = Intent()
        intent.component = componetName

        startActivity(intent)
    }

    private fun startVpn(c: Config, position: Int) {
        try {
            serverModel.vpnStart = true
            OpenVpnApi.startVpn(
                requireContext(),
                c.config,
                "wusui",
                c.users[position],
                c.pass[position]
            )

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    private fun initView() {
        if (activity is MainActivity) {
            serverModel.getConfig()
        }

        serverModel.config.observe(viewLifecycleOwner, { c ->
            val config = c.getOrNull()
            if (config == null) {
                Alerter.create(requireActivity())
                    .setBackgroundColorInt(Colors.colorSkin)
                    .setTitle("获取配置文件失败！").show()
                return@observe
            }
            val layoutManagers =
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            val serverListAdapter = ServerListAdapter(config.serverlist)
            binding.recyclerView.layoutManager = layoutManagers
            layoutManagers.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            binding.recyclerView.adapter = serverListAdapter
            serverListAdapter.setItemClickListener(object :
                ServerListAdapter.MainItemClickListener {
                override fun itemClick(position: Int) {
                    if (serverModel.vpnStart) {
                        confirmDisconnect()
                    } else {
                        preparevpn(config, position)
                    }
                }
            })

        })

    }


}

