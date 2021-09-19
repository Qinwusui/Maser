package com.wusui.server.utils.wiki

import retrofit2.Call
import retrofit2.http.GET

interface WikiService {
    @GET("wiki")
    fun getUrl(): Call<String>
}