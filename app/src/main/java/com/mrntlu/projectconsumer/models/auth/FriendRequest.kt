package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.models.main.review.Author

data class FriendRequest(
    @SerializedName("_id")
    val id: String,

    @SerializedName("is_ignored")
    val isIgnored: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    val sender: Author,
    val receiver: Author,
)
