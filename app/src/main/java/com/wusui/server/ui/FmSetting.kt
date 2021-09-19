package com.wusui.server.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.tapadoo.alerter.Alerter
import com.wusui.server.R
import com.wusui.server.activity.Launcher
import com.wusui.server.beans.Colors
import com.wusui.server.beans.Login
import com.wusui.server.model.SettingModel
import com.wusui.server.utils.JoinQQ
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


/**
 * @author wusui
 */
class FmSetting : PreferenceFragmentCompat() {
    private val settingModel by viewModels<SettingModel>()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aboutTest = findPreference<Preference>("aboutTest")!!
        aboutTest.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialDialog.Builder(requireContext())
                .title("关于内测")
                .content(
                    """
                    先简单介绍一下内测机制
                    1.所有内测中的功能都可能会在未来某个版本移除
                    2.内测中的功能仅提供给内测成员使用，无内测资格不能使用
                    3.想要成为内测成员的必须有能够承担应用程序崩溃的风险的能力
                    4.内测资格请找开发者申请，开发者同意后会邀请你进群测试
                """.trimIndent()
                )
                .positiveText("好的")
                .show()
            true
        }
        val backupTr = findPreference<Preference>("backupTr")!!
        backupTr.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (Login.qq == null) {
                Alerter.create(requireActivity())
                    .setTitle("你需要登录之后才能使用内测功能！")
                    .setBackgroundColorInt(Colors.colorSkin)
                    .show()
            } else {
                MaterialDialog.Builder(requireContext())
                    .title("请输入内测码")
                    .input(
                        "", "", true
                    ) { _, input ->
                        val code = input.toString()
                        settingModel.setTest(Login.qq!!, code)
                    }
                    .show()
            }
            true
        }
        settingModel.test.observe(viewLifecycleOwner, {
            val test = it.getOrNull()
            if (test != null && test.isTest) {
                MaterialDialog.Builder(requireContext())
                    .title("提示！")
                    .content(
                        """
                        1.内测版本无联机功能！无联机功能！
                        2.所有UI均使用JetPack Compose组件编写
                        3.如果想退出体验，请在体验版本的设置页点击退出即可！
                    """.trimIndent()
                    )
                    .onPositive { d, w ->
                        SPUtils.getInstance().put("www", "test")
                        val intent = Intent(requireActivity(), Launcher::class.java)
                        intent.putExtra("ww", "ads")
                        startActivity(intent)
                    }
                    .positiveText("我已知晓")
                    .negativeText("我不想使用")
                    .show()
            } else {
                Alerter.create(requireActivity())
                    .setTitle("你不是内测成员")
                    .setBackgroundColorInt(Colors.colorSkin)
                    .show()
            }
        })
        val scape = findPreference<Preference>("scapes")!!
        scape.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            BottomSheet.BottomListSheetBuilder(requireContext())
                .setTitle("浏览方式")
                .addItem("横屏浏览")
                .addItem("纵屏浏览")
                .setOnSheetItemClickListener { dialog, _, position, _ ->
                    when (position) {
                        0 -> {
                            SPUtils.getInstance().put("scape", "land")
                            dialog.dismiss()
                        }
                        1 -> {
                            dialog.dismiss()
                            SPUtils.getInstance().put("scape", "por")
                        }
                    }
                }
                .build()
                .show()
            true
        }
    }


}
