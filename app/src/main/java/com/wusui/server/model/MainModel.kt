package com.wusui.server.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.blankj.utilcode.util.DeviceUtils
import com.wusui.server.utils.Repository

class MainModel : ViewModel() {
    private val updateData = MutableLiveData<Any?>()
    val tablist = mutableListOf<String>()
    val update = updateData.switchMap {
        Repository.getUpdate()
    }

    fun initLiveData() {
        updateData.value = updateData.value
        announceMsg.value = announceMsg.value
        senMsg.value = senMsg.value
    }

    private val announceMsg = MutableLiveData<Any?>()
    val annouce = announceMsg.switchMap {
        Repository.getAnnounce(DeviceUtils.getAndroidID())
    }
    private val senMsg = MutableLiveData<Any?>()
    val sentence = senMsg.switchMap {
        Repository.getSen()
    }
}