package com.wusui.server.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.SPUtils
import com.wusui.server.R
import java.util.*
import kotlin.concurrent.schedule

/**
 * @author wusui
 */
class Launcher : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.launch_layout)
        setContent {
            InitView()
            if (SPUtils.getInstance()
                    .getString("www") != "test" && intent.getStringExtra("ww") != "ads"
            ) {
                home()
            } else {
                startFirstScreen()
            }
        }

    }

    private fun home() {
        Timer().schedule(1000) {
            val intent = Intent(this@Launcher, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun startFirstScreen() {
        Timer().schedule(1000) {
            val intent = Intent(this@Launcher, Firstuse::class.java)
            startActivity(intent)
            finish()
        }
    }


}

@Composable
fun InitView() {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.ic_icon),
                modifier = Modifier.size(width = 70.dp, height = 70.dp),
                contentDescription = null
            )
            Column {
                Text(
                    modifier = Modifier.padding(start = 10.dp, bottom = 5.dp),
                    text = "Maser",
                    fontSize = 45.sp,
                )
                Text(
                    text = "Produced By 琴六岁",
                )

            }
        }

    }

}
