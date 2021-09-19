package com.wusui.server.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.wusui.server.utils.Repository

class ServerModel : ViewModel() {
    private val configs = MutableLiveData<Any?>()
    var vpnStart: Boolean = false
    var strextra: String? = ""
    private val state = MutableLiveData<String>()

    val config = configs.switchMap {
        Repository.getConfig()
    }

    fun getConfig() {
        configs.value = configs.value
        state.value = state.value

    }

    fun isLogin() {

    }

}