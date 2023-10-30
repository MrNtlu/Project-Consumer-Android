package com.mrntlu.projectconsumer.models.main.review.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateReviewBody(
    val id: String,
    val review: String?,
    val star: Int?,
)

data class VoteReviewBody(
    val id: String,

    @SerializedName("is_like")
    val isLike: Boolean
)