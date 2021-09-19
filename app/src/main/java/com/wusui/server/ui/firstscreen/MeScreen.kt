package com.wusui.server.ui.firstscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ToastUtils
import com.wusui.server.R
import com.wusui.server.ui.firstscreen.viewmodel.MeModel

@Composable
fun MeScreen(meModel: MeModel = viewModel()) {
//    meModel.refresh()
//    val data = meModel.login.observeAsState()
//    val loginData = data.value?.getOrNull()
//    if (loginData != null) {
//        CircularProgressIndicator(color = Color(R.color.shuiyaqing))
//    }
    val openDialog = remember {
        mutableStateOf(false)
    }
    Image(
        painter = painterResource(id = R.drawable.ic_balloon),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 20.dp, top = 20.dp)
            .clickable {
                openDialog.value = true
            }
    )
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = true },
            text = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    Text(text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 40.sp)) {
                            append("请")
                        }
                        append("输入你的QQ与验证码")
                    })
                    val qq = remember {
                        mutableStateOf("")
                    }
                    OutlinedTextField(value = qq.value, onValueChange = {
                        qq.value = it
                        ToastUtils.showShort(it)

                    }, singleLine = true, modifier = Modifier
                        .fillMaxWidth(), label = { Text(text = "请输入QQ号") })
                    val code = remember {
                        mutableStateOf("")
                    }

                    OutlinedTextField(value = code.value, onValueChange = {
                        code.value = it
                        ToastUtils.showShort(it)
                    }, singleLine = true, label = { Text(text = "请输入验证码") })
                }

            },
            confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(text = "好")
                }
            }, dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}
