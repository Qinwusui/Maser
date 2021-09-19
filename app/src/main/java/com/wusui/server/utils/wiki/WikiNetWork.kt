package com.wusui.server.utils.wiki

import com.wusui.server.utils.Api
import retrofit2.await

object WikiNetWork {
    suspend fun getUrl() = Api.create(WikiService::class.java).getUrl().await()
}