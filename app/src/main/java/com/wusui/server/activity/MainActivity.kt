package com.wusui.server.activity

import android.Manifest
import android.annotation.SuppressLint
import android.net.VpnService
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.tapadoo.alerter.Alerter
import com.wusui.server.MyApplication
import com.wusui.server.R
import com.wusui.server.adpters.MyFragmentAdapter
import com.wusui.server.beans.Colors
import com.wusui.server.databinding.ActivityMainBinding
import com.wusui.server.model.MainModel
import com.wusui.server.utils.JoinQQ
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import solid.ren.skinlibrary.base.SkinBaseActivity
import java.io.*
import java.util.*

/**
 * @author wusui
 */
class MainActivity : SkinBaseActivity() {
    private var lastpress: Long = 0
    private lateinit var binding: ActivityMainBinding
    private val mainModel by viewModels<MainModel>()

    /**
     * @param savedInstanceState 实例状态
     */
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainModel.initLiveData()
        initView()
        initPermission()
        mainModel.annouce.observe(this, {
            val ann = it.getOrNull()
            if (ann != null) {
                if (ann.time != SPUtils.getInstance().getString("ann")) {
                    MaterialDialog.Builder(this)
                        .title(ann.title)
                        .content(ann.msg.trimIndent())
                        .negativeText("不再提示此公告")
                        .onNegative { _, _ ->
                            SPUtils.getInstance().put("ann", ann.time)
                        }
                        .positiveText("知道了")
                        .show()
                }
            }
        })
    }

    private fun initPermission() {

        if ("true" != SPUtils.getInstance().getString("PermissionGranted")) {
            val builder = MaterialDialog.Builder(this)
            builder.title("必需权限申请")
                .content(
                    """
                    Maser需要以下权限以保证正常工作
                    1.完全的网络访问权限
                    2.读写存储空间
                    3.控制震动
                    4.建立VPN隧道
                    5.获取IMEI(设备唯一识别码)
                    是否同意?
                    """.trimIndent()
                )
                .positiveText("同意")
                .onPositive { _: MaterialDialog?, _: DialogAction? -> permission() }
                .negativeText("拒绝")
                .onNegative { _: MaterialDialog?, _: DialogAction? ->
                    ToastUtils.showShort("好吧...")
                }
                .cancelable(false)
                .show()
        }
    }

    private fun permission() {
        val permission = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_PHONE_STATE,
        )
        PermissionUtils.permission(*permission)
            .callback(object : PermissionUtils.SimpleCallback {
                override fun onGranted() {
                    SPUtils.getInstance().put("PermissionGranted", "true")
                    val intent = VpnService.prepare(MyApplication.context)
                    if (intent != null) {
                        startActivityForResult(intent, 1)
                    }
                }

                override fun onDenied() {
                }
            })
            .request()

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        setMargins(binding.actionBarEx, 0, BarUtils.getStatusBarHeight(), 0, 0)
        Colors.colorSkin = ColorUtils.getColor(R.color.dxtitle)
        binding.actionBarEx.findViewById<View>(R.id.forground_draw)
            .setBackgroundColor(Colors.colorSkin)
        BarUtils.setStatusBarColor(this, Colors.colorSkin)
        BarUtils.setStatusBarLightMode(window, true)
        val viewPager2 = binding.viewPager
        viewPager2.adapter = MyFragmentAdapter(this)
        viewPager2.isUserInputEnabled = false
        binding.mainLayout.setBackgroundColor(ColorUtils.getColor(R.color.dxback))
        binding.viewPager.adapter = viewPager2.adapter
        binding.viewPager.offscreenPageLimit = 4

        val tabLayoutMediator = TabLayoutMediator(
            binding.tab, binding.viewPager
        ) { tab, position ->
            val list = mainModel.tablist
            mainModel.tablist.addAll(arrayOf("主页", "Wiki", "我的", "设置"))
            tab.text = list[position]
        }
        tabLayoutMediator.attach()

        setMargins(
            binding.actionBarEx.foregroundLayer.findViewById(R.id.actionBar_foreg_title),
            20,
            0,
            0,
            0
        )
        setMargins(
            binding.actionBarEx.foregroundLayer.findViewById(R.id.actionBar_foreg_hint),
            20,
            0,
            0,
            0
        )
        val forg_title = binding.actionBarEx.findViewById<TextView>(R.id.actionBar_foreg_title)
        forg_title.text = "Maser"
        forg_title.setTextColor(ColorUtils.getColor(R.color.hei))
        binding.actionBarEx.findViewById<TextView>(R.id.actionBar_foreg_hint)
            .setTextColor(ColorUtils.getColor(R.color.hei))

        binding.actionBarEx.findViewById<ImageView>(R.id.actionBar_background_img)
            .setOnClickListener {
                if (AppUtils.isAppInstalled("com.and.games505.TerrariaPaid")) {
                    AppUtils.launchApp("com.and.games505.TerrariaPaid")
                } else {
                    Alerter.create(this).setTitle("你还没有安装Terraria或者Terraria已被冻结！")
                        .setBackgroundColorInt(Colors.colorSkin).show()
                }
            }
        binding.actionBarEx.findViewById<ImageView>(R.id.actionBar_background_img).scaleType =
            ImageView.ScaleType.CENTER_INSIDE

        Glide.with(binding.actionBarEx.findViewById<ImageView>(R.id.actionBar_background_img))
            .load(R.drawable.ic_balloon)
            .into(binding.actionBarEx.findViewById(R.id.actionBar_background_img))

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewPager2.isUserInputEnabled = position != 1
                if (position == 1 && SPUtils.getInstance().getString("scape") == "land") {
                    binding.actionBarEx.isVisible = false
                    BarUtils.setStatusBarVisibility(this@MainActivity, false)
                    ScreenUtils.setLandscape(this@MainActivity)
                } else {
                    ScreenUtils.setPortrait(this@MainActivity)
                    binding.actionBarEx.isVisible = true

                }
            }
        })

        mainModel.update.observe(this, { u ->
            val update = u.getOrNull()
            if (u.isSuccess && update != null && AppUtils.getAppVersionName() != update.updateVersion) {
                val builder = MaterialDialog.Builder(this@MainActivity)
                    .title("有更新啦!")
                    .content("新版本:${update.updateVersion}\n更新日志:${update.updateMessage}")
                    .positiveText("更新")
                    .onPositive { _, _ ->
                        JoinQQ().joinQQGroup()
                        ToastUtils.showLong("请在群文件内寻找最新版本更新）")
                        finish()
                    }
                    .negativeText("不更新")
                builder.onNegative { d, _ ->
                    if (update.isMustUpdate) {
//                        ToastUtils.showShort("这次的更新推荐你更新呢~")
                        ToastUtils.showShort("此次更新为强制更新，请在更新后继续使用~")
                        builder.show()
                    } else {
                        ToastUtils.showShort("好吧...")
                    }
                }
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .show()
            }
        })
        mainModel.sentence.observe(this, { sen ->
            val sentence = sen.getOrThrow()
            this.findViewById<TextView>(R.id.actionBar_foreg_hint).text =
                sentence.content
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.viewPager.currentItem == 1) {
                if (binding.root.rootView.findViewById<WebView>(R.id.webview).url == "https://terraria.fandom.com/zh/wiki/Terraria_Wiki") {
                    exit()
                } else {
                    ToastUtils.showShort("正在返回上一页面")
                    binding.root.rootView.findViewById<WebView>(R.id.webview).goBack()
                }
            } else {
                exit()
            }
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun exit() {
        if (System.currentTimeMillis() - lastpress > 2000) {
            Alerter.create(this).setBackgroundColorInt(Colors.colorSkin)
                .setTitle("再按一次退出程序").show()

            lastpress = System.currentTimeMillis()
        } else {
            AppUtils.exitApp()
        }
    }

    companion object {
        /**
         * @param v      view
         * @param left   左部
         * @param top    上部
         * @param right  右部
         * @param bottom 底部
         */
        fun setMargins(v: View, left: Int, top: Int, right: Int, bottom: Int) {
            if (v.layoutParams is MarginLayoutParams) {
                val p = v.layoutParams as MarginLayoutParams
                p.setMargins(left, top, right, bottom)
                v.requestLayout()
            }
        }
    }

}