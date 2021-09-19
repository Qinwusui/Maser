package com.wusui.server.ui.firstscreen.viewmodel

import androidx.lifecycle.ViewModel

class MeModel : ViewModel() {
    //屏幕旋转会导致登陆状态丢失，必须进行保存
//    var isLogin=false
//    var qq=""
//
//    private val logindata=MutableLiveData<Any?>()
//    val login=logindata.switchMap {
//        Repository.login()
//    }
//    fun refresh(){
//        logindata.value=logindata.value
//    }
}