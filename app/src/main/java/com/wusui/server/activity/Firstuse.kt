package com.wusui.server.activity

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.tapadoo.alerter.Alerter
import com.wusui.server.R
import com.wusui.server.ui.firstscreen.Home

class Firstuse : ComponentActivity() {
    var time: Long = 0

    @ExperimentalCoilApi
    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colors.background) {
                Home()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK ->
                if (System.currentTimeMillis() - time < 2000) {
                    finish()
                } else {
                    Alerter.create(this).setDuration(2000).setBackgroundColorRes(R.color.shuiyaqing)
                        .setTitle("再按一次退出程序~").show()
                    time = System.currentTimeMillis()
                }
        }
        return true
    }
}