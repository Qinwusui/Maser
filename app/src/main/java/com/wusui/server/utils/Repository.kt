package com.wusui.server.utils

import androidx.lifecycle.liveData
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.SPUtils
import com.wusui.server.beans.Login
import com.wusui.server.beans.MyData
import com.wusui.server.utils.announcement.AnnounceNetWork
import com.wusui.server.utils.config.ConfigNetWork
import com.wusui.server.utils.login.LoginNetWork
import com.wusui.server.utils.sentence.SentenceNetWork
import com.wusui.server.utils.test.TestNetWork
import com.wusui.server.utils.update.UpdateNetWork
import com.wusui.server.utils.wiki.WikiNetWork
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object Repository {
    fun getTest(qq: String, code: String) = fire(Dispatchers.IO) {
        val test = TestNetWork.test(qq, code)
        Result.success(test)
    }

    fun getConfig() = fire(Dispatchers.IO) {
        val config = ConfigNetWork.getConfig()
        Result.success(config)
    }

    fun getSen() = fire(Dispatchers.IO) {
        val sen = SentenceNetWork.getSen()
        Result.success(sen)
    }

    fun getUpdate() = fire(Dispatchers.IO) {
        val update = UpdateNetWork.getUpdate()
        if (update.updateVersion != "") {

            Result.success(update)
        } else {
            Result.failure(Exception("null"))
        }
    }

    fun getAnnounce(id: String) = fire(Dispatchers.IO) {
        val announce = AnnounceNetWork.getAnnounce(id)
        if (announce.isSend) {
            Result.success(announce)
        } else {
            Result.failure(Exception("Null"))
        }
    }

    fun remove() {
        SPUtils.getInstance().remove("UserNum")
        SPUtils.getInstance().remove("vcode")
        SPUtils.getInstance().remove("name")
        Login.name = "无名氏"
        Login.code = null
        Login.qq = null
    }

    fun login() = fire(Dispatchers.IO) {
        //打开App自动登陆
        val qq = SPUtils.getInstance().getString("UserNum")
        val code = SPUtils.getInstance().getString("vcode")
        val name = SPUtils.getInstance().getString("name")
        val token = SPUtils.getInstance().getString("rtok")
        if (qq.isNotEmpty() && code.isNotEmpty() && name.isNotEmpty()&&token.isNotEmpty()) {

            val login = LoginNetWork.login(qq, code, token)
            if (login.isLogin) {
                Login.qq = qq
                Login.code = code
                Login.name = name
                Result.success(login)
            } else {
                Result.success(MyData(false, "未登录"))
            }
        } else {
            Result.success(MyData(false, "未登录"))
        }
    }

    fun loginha(qq: String, code: String) = fire(Dispatchers.IO) {
        //手动登陆
        val token = EncryptUtils.encryptSHA1ToString(System.currentTimeMillis().toString())
        SPUtils.getInstance().put("rtok", token)
        val login = LoginNetWork.login(qq, code, token)
        if (login.isLogin) {
            Login.qq = qq
            Login.code = code
            Result.success(login)
        } else {
            Result.success(MyData(false, "登陆失败"))
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }

    fun getWikiUrl() = fire(Dispatchers.IO) {
        val url = WikiNetWork.getUrl()
        Result.success(url)
    }


}