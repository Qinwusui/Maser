package com.wusui.server.utils.config

import com.wusui.server.utils.Api
import retrofit2.await

object ConfigNetWork {
    private val config = Api.create<ConfigService>()
    suspend fun getConfig() = config.getConfig().await()
}