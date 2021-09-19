package com.wusui.server.ui.firstscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blankj.utilcode.util.ColorUtils
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.wusui.server.R
import com.wusui.server.ui.firstscreen.viewmodel.MainModel

@ExperimentalPagerApi
@Composable
fun ServerScreen() {
    val viewModel: MainModel = viewModel()
    viewModel.getConfig()
    val config = viewModel.config.observeAsState()
    val data = config.value?.getOrNull()
    if (data == null) {
        CircularProgressIndicator(color = Color(ColorUtils.getRandomColor(false)))
    }
    val pageState = rememberPagerState(
        pageCount = 4,
        initialOffscreenLimit = 4,
        infiniteLoop = true,
        initialPage = 0
    )
    HorizontalPager(
        state = pageState, modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) { index ->
        Image(
            painter = painterResource(id = R.drawable.ic_balloon),
            contentScale = ContentScale.Inside,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(text = "广告${index + 1}")
        }


    }


    data?.serverlist?.forEachIndexed { index, s ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = 1.dp,

            ) {
            Column(horizontalAlignment = Alignment.End) {
                Image(
                    painter = painterResource(id = R.drawable.ic_balloon),
                    contentDescription = s,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Text(text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontSize = 30.sp)
                    ) {
                        append(s[0])
                    }
                    append(s.removePrefix(s[0].toString()))
                }, modifier = Modifier.padding(end = 10.dp))
            }
        }

    }


}