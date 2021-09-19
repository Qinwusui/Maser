package com.wusui.server.utils.tk

import com.wusui.server.beans.MyData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TkService {
    @GET("rtok")
    fun getTk(@Query("qq") qq: String, @Query("code") code: String): Call<MyData>
}