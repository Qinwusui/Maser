package com.wusui.server.utils.update

import com.wusui.server.beans.Update
import retrofit2.Call
import retrofit2.http.GET

interface UpdateService {
    @GET("update")
    fun getUpdate(): Call<Update>
}