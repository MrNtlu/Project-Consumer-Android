package com.mrntlu.projectconsumer.models.main.review

import com.google.gson.annotations.SerializedName

data class ReviewDetails(
    val author: Author,
    val star: Int,
    val review: String,
    var popularity: Int,
    val likes: ArrayList<Author>,

    @SerializedName("is_author")
    val isAuthor: Boolean,

    @SerializedName("is_liked")
    var isLiked: Boolean,

    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userID: String,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("content_id")
    val contentID: String,

    @SerializedName("content_external_id")
    val contentExternalID: String?,

    @SerializedName("content_external_int_id")
    val contentExternalIntID: Int?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    val content: ReviewContent,
) {
    constructor(): this(
        Author(), 0, "", 0, arrayListOf(), false, false, "",  "",
        "", "", null, null,  "", "", ReviewContent()
    )
}