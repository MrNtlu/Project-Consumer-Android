package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName

data class User(
//    val app_notification: Boolean,
//    val mail_notification: Boolean,
    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("is_oauth")
    val isOAuth: Boolean,

    @SerializedName("is_premium")
    val isPremium: Boolean,

    @SerializedName("oauth_type")
    val oauthType: Int?,

    val email: String,
    val image: String?,
    val username: String
)
