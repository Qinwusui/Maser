package com.wusui.server.utils.announcement

import com.wusui.server.beans.AnnounceMessage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AnnounceService {
    @GET("announcement")

    fun getAnnounce(@Query("id") deviceId: String): Call<AnnounceMessage>
}