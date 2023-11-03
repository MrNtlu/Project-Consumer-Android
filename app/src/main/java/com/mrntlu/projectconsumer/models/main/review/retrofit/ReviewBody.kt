package com.mrntlu.projectconsumer.models.main.review.retrofit

import com.google.gson.annotations.SerializedName

data class ReviewBody(
    @SerializedName("content_id")
    val contentId: String,

    @SerializedName("content_external_id")
    val contentExternalId: String?,

    @SerializedName("content_external_int_id")
    val contentExternalIntId: Int?,

    @SerializedName("content_type")
    val contentType: String,

    val star: Int,
    val review: String,
)
