package com.wusui.server.utils.login

import com.wusui.server.beans.MyData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginService {
    @GET("up")
    fun getData(
        @Query("qq") qq: String,
        @Query("code") code: String,
        @Query("token") token: String
    ): Call<MyData>
}