package com.wusui.server.utils.update

import com.wusui.server.utils.Api
import retrofit2.await

object UpdateNetWork {
    private val update = Api.create(UpdateService::class.java)
    suspend fun getUpdate() = update.getUpdate().await()
}