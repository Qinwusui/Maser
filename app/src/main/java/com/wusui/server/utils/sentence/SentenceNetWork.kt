package com.wusui.server.utils.sentence

import com.wusui.server.utils.Api
import retrofit2.await

object SentenceNetWork {
    suspend fun getSen() = Api.createSen(SentenceService::class.java).getSen().await()

}