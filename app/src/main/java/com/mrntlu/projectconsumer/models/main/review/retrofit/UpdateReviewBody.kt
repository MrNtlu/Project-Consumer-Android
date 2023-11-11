package com.mrntlu.projectconsumer.models.main.review.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateReviewBody(
    val id: String,
    val review: String?,
    val star: Int?,

    @SerializedName("is_spoiler")
    val isSpoiler: Boolean?,
)