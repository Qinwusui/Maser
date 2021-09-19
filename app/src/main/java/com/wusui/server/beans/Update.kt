package com.wusui.server.beans

import com.google.gson.annotations.SerializedName


data class Update(
    val url: String = "",
    val updateMessage: String = "",
    val updateTime: String = "",
    val updateVersion: String = "",
    val isMustUpdate: Boolean = true
)

data class Config(
    val config: String,
    val users: List<String>,
    val pass: List<String>,
    val serverlist: List<String>
)

data class MyData(val isLogin: Boolean, val Msg: String)

data class AnnounceMessage(
    val msg: String,
    val time: String,
    val isSend: Boolean,
    val title: String
)

data class EveryDayData(
    @SerializedName("ad_url")
    val adUrl: Any?,
    @SerializedName("assign_date")
    val assignDate: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("daily_audio_urls")
    val dailyAudioUrls: Any?,
    @SerializedName("id")
    val id: String,
    @SerializedName("join_num")
    val joinNum: Int,
    @SerializedName("origin_img_urls")
    val originImgUrls: List<String>,
    @SerializedName("poster_img_urls")
    val posterImgUrls: List<String>,
    @SerializedName("share_img_urls")
    val shareImgUrls: List<String>,
    @SerializedName("share_url")
    val shareUrl: String,
    @SerializedName("share_urls")
    val shareUrls: ShareUrls,
    @SerializedName("track_object")
    val trackObject: TrackObject,
    @SerializedName("translation")
    val translation: String
) {
    data class ShareUrls(
        @SerializedName("qzone")
        val qzone: String,
        @SerializedName("shanbay")
        val shanbay: String,
        @SerializedName("wechat")
        val wechat: String,
        @SerializedName("wechat_user")
        val wechatUser: String,
        @SerializedName("weibo")
        val weibo: String
    )

    data class TrackObject(
        @SerializedName("code")
        val code: String,
        @SerializedName("object_id")
        val objectId: Int,
        @SerializedName("share_url")
        val shareUrl: String
    )
}


data class TestData(val isTest: Boolean, val msg: String)