package com.wusui.server.model

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.wusui.server.beans.Login
import com.wusui.server.ui.FmServer
import com.wusui.server.utils.Repository
import com.wusui.server.utils.tk.TkNetWork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class UserModel : ViewModel() {
    var qq = MutableLiveData<String>()
    var code = MutableLiveData<String>()
    var name = MutableLiveData<String>()
    private val loginMsg = MutableLiveData<Any?>()
    val mHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                if (msg.obj.toString() != SPUtils.getInstance().getString("rtok")) {
                    ToastUtils.showLong("验证信息已过期，请重新登陆")
                    FmServer().stopVpn()
                    Repository.remove()
                }
            }
            super.handleMessage(msg)
        }
    }
    val login = loginMsg.switchMap {
        Repository.login()
    }

    fun getYTk() {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                viewModelScope.launch(Dispatchers.IO) {
                    val qq = SPUtils.getInstance().getString("UserNum")
                    val code = SPUtils.getInstance().getString("vcode")
                    if (qq.isNotEmpty() && code.isNotEmpty()) {
                        val token = TkNetWork.getTk(qq, code)
                        if (token.isLogin) {
                            val message = Message()
                            message.what = 1
                            message.obj = token.Msg
                            mHandler.sendMessage(message)
                        }
                    } else {
                        return@launch
                    }
                }
            }
        }
        timer.schedule(timerTask, 1000, 40000)//延时1s，每隔500毫秒执行一次run方法
    }

    fun getLogin() {
        loginMsg.value = loginMsg.value
    }

    fun getCoro() = viewModelScope
    fun setLogin(qq: String, code: String) {
        this.qq.value = qq
        this.code.value = code
        loginha.value = loginha.value
    }

    fun removeLoginData() {
        SPUtils.getInstance().remove("UserNum")
        SPUtils.getInstance().remove("vcode")
        SPUtils.getInstance().remove("name")
        Login.name = "无名氏"
        Login.code = null
        Login.qq = null
    }

    private val loginha = MutableLiveData<Any?>()
    val loginHa = loginha.switchMap {
        Repository.loginha(qq.value.toString(), code.value.toString())
    }
}