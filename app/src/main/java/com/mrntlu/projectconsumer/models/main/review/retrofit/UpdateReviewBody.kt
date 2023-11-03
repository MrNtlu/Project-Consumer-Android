package com.mrntlu.projectconsumer.models.main.review.retrofit

data class UpdateReviewBody(
    val id: String,
    val review: String?,
    val star: Int?,
)