package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateNotification(
    @SerializedName("friend_request")
    val friendRequest: Boolean,

    @SerializedName("review_likes")
    val reviewLikes: Boolean,
)
