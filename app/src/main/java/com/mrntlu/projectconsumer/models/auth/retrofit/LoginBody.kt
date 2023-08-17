package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class LoginBody(
    @SerializedName("email_address")
    val email: String,

    @SerializedName("fcm_token")
    val fcmToken: String?,

    val password: String
)
