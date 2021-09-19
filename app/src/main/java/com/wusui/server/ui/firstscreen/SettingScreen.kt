package com.wusui.server.ui.firstscreen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SPUtils
import com.wusui.server.MyApplication
import com.wusui.server.activity.Launcher

@Composable
fun SettingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            elevation = 2.dp,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .clickable {
                    SPUtils
                        .getInstance()
                        .remove("www")
                    val intent = Intent(MyApplication.context, Launcher::class.java)
                    ActivityUtils.startActivity(intent)
                }
                .background(Color.Transparent)
                .height(50.dp)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(text = "退出测试页", fontSize = 30.sp)
            }
        }
    }
}