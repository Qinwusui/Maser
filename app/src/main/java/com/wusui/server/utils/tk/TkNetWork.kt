package com.wusui.server.utils.tk

import com.wusui.server.utils.Api
import retrofit2.await

object TkNetWork {
    private val data = Api.create<TkService>()
    suspend fun getTk(qq: String, code: String) = data.getTk(qq, code).await()
}