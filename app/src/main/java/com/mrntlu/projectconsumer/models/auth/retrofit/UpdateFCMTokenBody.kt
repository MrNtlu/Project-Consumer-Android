package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateFCMTokenBody(
    @SerializedName("fcm_token")
    val fcmToken: String,
)