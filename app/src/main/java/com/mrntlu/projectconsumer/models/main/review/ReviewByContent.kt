package com.mrntlu.projectconsumer.models.main.review

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewByContent(
    val author: Author,
    val star: Int,
    val review: String,
    val popularity: Int,
    val likes: ArrayList<String>,

    @SerializedName("is_author")
    val isAuthor: Boolean,

    @SerializedName("is_liked")
    val isLiked: Boolean,

    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userID: String,

    @SerializedName("content_id")
    val contentID: String,

    @SerializedName("content_external_id")
    val contentExternalID: String?,

    @SerializedName("content_external_int_id")
    val contentExternalIntID: Int?,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    val content: ReviewContent,
): Parcelable, DiffUtilComparison<ReviewByContent> {
    constructor(): this(
        Author(), 0, "", 0, arrayListOf(), false, false, "",  "",
        "", null, null, "", "", "", ReviewContent()
    )

    override fun areItemsTheSame(newItem: ReviewByContent): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: ReviewByContent): Boolean {
        return when {
            id != newItem.id -> false
            star != newItem.star -> false
            review != newItem.review -> false
            isLiked != newItem.isLiked -> false
            popularity != newItem.popularity -> false
            contentID != newItem.contentID -> false
            likes.toSet() != newItem.likes.toSet() -> false
            contentExternalID != newItem.contentExternalID -> false
            contentExternalIntID != newItem.contentExternalIntID -> false
            content.titleOriginal != newItem.content.titleOriginal -> false
            else -> true
        }
    }
}

@Parcelize
data class ReviewContent(
    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageURL: String,
): Parcelable {
    constructor(): this("", "", "")
}