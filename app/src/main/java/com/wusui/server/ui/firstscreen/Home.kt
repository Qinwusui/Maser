package com.wusui.server.ui.firstscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.wusui.server.R
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@ExperimentalPagerApi
@Composable
fun Home() {

    val titles = listOf("服务器", "Wiki", "我的", "设置")
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = titles.size,
        initialPage = 0,
        infiniteLoop = true,
        initialOffscreenLimit = titles.size
    )
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(Color.Transparent, darkIcons = true)
    }

    Scaffold(topBar = {

        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            Row(modifier = Modifier.padding(start = 10.dp, top = 25.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_balloon),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 40.sp)) {
                            append("M")
                        }
                        append("aser")
                    }, modifier = Modifier.padding(top = 10.dp))
                    Text(text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 10.sp)) {
                            append(
                                "Produced BY 琴 七 岁"
                            )
                        }
                    })
                }
            }

        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.white)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth(),
                edgePadding = 16.dp
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.scrollToPage(index) } },

                        text = {
                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontSize = 30.sp,
                                        )
                                    ) {
                                        append(title[0])
                                    }
                                    append(title.removePrefix(title[0].toString()))
                                }
                            )
                        })
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1F)) { index ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (index) {
                        0 -> ServerScreen()
                        1 -> WikiScreen()
                        2 -> MeScreen()
                        3 -> SettingScreen()
                    }
                }
            }
        }
    }


}




