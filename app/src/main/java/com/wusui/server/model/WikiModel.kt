package com.wusui.server.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.wusui.server.utils.Repository

class WikiModel : ViewModel() {
    private val url = MutableLiveData<Any?>()
    val wiki = url.switchMap {
        Repository.getWikiUrl()
    }

    fun setDefaultValue() {
        url.value = url.value
    }
}