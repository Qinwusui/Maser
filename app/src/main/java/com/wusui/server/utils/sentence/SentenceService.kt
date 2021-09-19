package com.wusui.server.utils.sentence

import com.wusui.server.beans.EveryDayData
import retrofit2.Call
import retrofit2.http.GET

interface SentenceService {
    @GET("weapps/dailyquote/quote/")
    fun getSen(): Call<EveryDayData>
}