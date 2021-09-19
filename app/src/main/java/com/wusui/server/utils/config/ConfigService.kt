package com.wusui.server.utils.config

import com.wusui.server.beans.Config
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ConfigService {
    @GET("configs")
    fun getConfig(@Query("token") token: String = "Qinsansui233...@"): Call<Config>
}