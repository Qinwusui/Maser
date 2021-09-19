package com.wusui.server.ui.firstscreen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.wusui.server.utils.Repository

class MainModel : ViewModel() {

    private val configs = MutableLiveData<Any?>()

    private val states = MutableLiveData<String>()

    val config = configs.switchMap {
        Repository.getConfig()
    }

    fun getConfig() {
        configs.value = configs.value
        states.value = states.value

    }

    var state = ""
}