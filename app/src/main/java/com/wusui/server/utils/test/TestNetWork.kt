package com.wusui.server.utils.test

import com.wusui.server.utils.Api
import retrofit2.await

object TestNetWork {
    private val data = Api.create(TestService::class.java)
    suspend fun test(qq: String, code: String) = data.test(qq, code).await()
}