package com.wusui.server.utils.announcement

import com.wusui.server.utils.Api
import retrofit2.await

object AnnounceNetWork {
    private val config = Api.create<AnnounceService>()
    suspend fun getAnnounce(deviceId: String) = config.getAnnounce(deviceId).await()
}