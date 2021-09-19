package com.wusui.server.utils.test

import com.wusui.server.beans.TestData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TestService {
    @GET("test")
    fun test(
        @Query("qq") qq: String,
        @Query("code") code: String,
        @Query("login") login: String = "Login"
    ): Call<TestData>
}