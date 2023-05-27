package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class RegisterBody(
    @SerializedName("email_address")
    val email: String,

    @SerializedName("fcm_token")
    val fcmToken: String,

    val username: String,
    val password: String,
    val image: String,
)
