package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName

data class BasicUserInfo(
    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("is_oauth")
    val isOAuth: Boolean,

    @SerializedName("is_premium")
    val isPremium: Boolean,

    @SerializedName("membership_type")
    val membershipType: Int,

    @SerializedName("oauth_type")
    val oauthType: Int?,

    @SerializedName("can_change_username")
    var canChangeUsername: Boolean,

    @SerializedName("app_notification")
    val appNotification: Notification,

    val email: String,
    val image: String?,
    val username: String
)

data class Notification(
    @SerializedName("friend_request")
    var friendRequest: Boolean,

    @SerializedName("review_likes")
    var reviewLikes: Boolean
)