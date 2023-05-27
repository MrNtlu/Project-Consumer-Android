package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class GoogleLoginBody(
    val token: String,

    val image: String,

    @SerializedName("fcm_token")
    val fcmToken: String,
)
