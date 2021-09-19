package com.wusui.server

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.xuexiang.xui.XUI

class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        XUI.init(this)
        XUI.debug(false)

    }
}