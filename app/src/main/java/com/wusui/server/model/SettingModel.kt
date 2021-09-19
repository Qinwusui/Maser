package com.wusui.server.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.wusui.server.utils.Repository

class SettingModel : ViewModel() {
    var qq = ""
    var code = ""
    private val testData = MutableLiveData<Any?>()
    val test = testData.switchMap {
        Repository.getTest(qq, code)
    }

    fun setTest(qq: String, code: String) {
        this.qq = qq
        this.code = code
        reset()
    }

    private fun reset() {
        testData.value = testData.value
    }
}