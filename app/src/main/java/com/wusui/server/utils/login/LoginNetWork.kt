package com.wusui.server.utils.login

import com.wusui.server.utils.Api
import retrofit2.await

object LoginNetWork {
    private val login = Api.create<LoginService>()
    suspend fun login(qq: String, code: String, token: String) =
        login.getData(qq, code, token).await()
}