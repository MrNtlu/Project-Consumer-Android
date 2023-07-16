package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class Log(
    @SerializedName("content_id")
    val contentId: String,

    @SerializedName("content_image")
    val contentImage: String,

    @SerializedName("content_title")
    val contentTitle: String,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("log_action")
    val logAction: String,

    @SerializedName("log_action_details")
    val logActionDetails: String,

    @SerializedName("log_type")
    val logType: String,
)