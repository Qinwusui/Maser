package com.wusui.server.ui

import android.annotation.SuppressLint
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.tapadoo.alerter.Alerter
import com.wusui.server.R
import com.wusui.server.activity.MainActivity
import com.wusui.server.beans.Colors
import com.wusui.server.beans.Login
import com.wusui.server.databinding.FragmentUserBinding
import com.wusui.server.model.UserModel
import com.wusui.server.utils.JoinQQ
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import solid.ren.skinlibrary.base.SkinBaseFragment
import java.util.*

/**
 * @author 琴五岁
 *
 */
class FmUser : SkinBaseFragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    val userModel by viewModels<UserModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }


    @SuppressLint("InflateParams")
    private fun initView() {
        if (activity is MainActivity) {
            userModel.getLogin()
        }
        userModel.getYTk()
        userModel.login.observe(viewLifecycleOwner, {
            val myData = it.getOrNull()
            if (myData != null) {
                if (myData.isLogin) {
                    Glide.with(binding.root)
                        .load("http://q1.qlogo.cn/g?b=qq&nk=${Login.qq}&s=100")
                        .into(binding.userIcon)
                    binding.username.text = Login.name
                    Alerter.create(requireActivity())
                        .setBackgroundColorInt(Colors.colorSkin)
                        .setTitle("登陆成功！欢迎 ${Login.name}~")
                        .show()
                } else {
                    binding.username.text = myData.Msg
                }
            }
        })

        binding.userIcon.scaleType = ImageView.ScaleType.CENTER_CROP

        binding.userIcon.setOnClickListener {
            if (Login.code == null || Login.qq == null) {
                val layoutInflater = LayoutInflater.from(requireContext())
                val layout = layoutInflater.inflate(R.layout.code_item, null)
                val dialog = MaterialDialog.Builder(requireContext())
                    .customView(layout, true)
                    .build()
                dialog.show()
                val imgtitle = layout.findViewById<ImageView>(R.id.imgtitle)
                val qq = layout.findViewById<TextInputEditText>(R.id.qqinput)
                val code = layout.findViewById<TextInputEditText>(R.id.codeinput)
                val what = layout.findViewById<Button>(R.id.what_code)
                val confirm = layout.findViewById<Button>(R.id.confirm)
                val titles = layout.findViewById<MaterialTextView>(R.id.texttitle)
                titles.setTextColor(Colors.colorSkin)
                titles.text = "验证码登录"
                Glide.with(imgtitle).load(R.drawable.ic_icon).into(imgtitle)
                what.setOnClickListener {
                    MaterialDialog.Builder(requireContext())
                        .title("关于验证码")
                        .content(
                            """
                    1.验证码功能是Maser为了提供给群内成员使用的功能。
                    2.去除QQ登录后，安装包体积能够明显减小。
                    3.验证码功能可以节约我的开发时间。
                """.trimIndent()
                        )
                        .negativeText("我知道了")
                        .onNegative { dia, _ ->
                            dia.dismiss()
                        }
                        .show()
                }
                confirm.setOnClickListener {
                    if (qq.text.toString().isEmpty()) {
                        qq.error = "请填写你的QQ号"

                    }
                    if (code.text.toString().isEmpty()) {
                        code.error = "请填写你的验证码"
                    } else {
                        userModel.setLogin(qq.text.toString(), code.text.toString())
                        dialog.dismiss()
                    }

                }


            } else {
                ToastUtils.showShort("你已经登录过了（")

            }
        }
        userModel.loginHa.observe(viewLifecycleOwner, {
            val login = it.getOrThrow()
            if (login.isLogin) {
                if (Login.name == "无名氏") {
                    MaterialDialog.Builder(requireContext())
                        .title("你的昵称？")
                        .input("", "") { dia, w ->
                            binding.username.text = w.toString()
                            SPUtils.getInstance().put("name", w.toString())
                            SPUtils.getInstance().put("UserNum", Login.qq)
                            SPUtils.getInstance().put("vcode", Login.code)
                            Glide.with(binding.userIcon)
                                .load("http://q1.qlogo.cn/g?b=qq&nk=${Login.qq}&s=100")
                                .into(binding.userIcon)
                            Alerter.create(requireActivity())
                                .setBackgroundColorInt(Colors.colorSkin)
                                .setTitle("欢迎 $w~")
                                .show()

                            Login.name = w.toString()
                        }
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .show()
                }

            } else {
                Alerter.create(requireActivity()).setBackgroundColorInt(Colors.colorSkin)
                    .setTitle("登录失败！请检查验证码是否输入正确或者账户是否已经拥有验证码！")
                    .show()
            }
        })

        binding.userIcon.setOnLongClickListener {
            if (Login.qq != null) {
                MaterialDialog.Builder(requireContext())
                    .title("提示")
                    .content("确定要登出么？你还记得你的密码么？")
                    .positiveText("确定")
                    .onPositive { dia, _ ->
                        userModel.removeLoginData()
                        Glide.with(binding.userIcon).load(R.drawable.ic_icon).into(binding.userIcon)
                        binding.username.text = Login.name
                        dia.dismiss()
                    }
                    .negativeText("点错了")
                    .show()
            }

            return@setOnLongClickListener true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}