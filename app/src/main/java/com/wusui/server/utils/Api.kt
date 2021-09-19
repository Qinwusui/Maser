package com.wusui.server.utils

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    //TODO 这里请改成自己的Url
    private const val BaseUrl = "http://example/"
    fun <T> createSen(serv: Class<T>): T = createRetrofit(SenUrl).create(serv)
    fun <T> create(serviceClass: Class<T>): T = createRetrofit(BaseUrl).create(serviceClass)
    inline fun <reified T> create(): T = create(T::class.java)
    //扇贝每日一句
    private const val SenUrl = "https://apiv3.shanbay.com/"

    fun createRetrofit(url: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(url)
        .build()
}

