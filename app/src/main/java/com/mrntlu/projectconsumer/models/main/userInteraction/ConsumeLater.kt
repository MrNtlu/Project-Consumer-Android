package com.mrntlu.projectconsumer.models.main.userInteraction

import com.google.gson.annotations.SerializedName

data class ConsumeLater(
    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userID: String,

    @SerializedName("content_id")
    val contentID: String,

    @SerializedName("content_external_id")
    val contentExternalID: String,

    @SerializedName("content_external_int_id")
    val contentExternalIntID: Int,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("self_note")
    val selfNote: String
)
