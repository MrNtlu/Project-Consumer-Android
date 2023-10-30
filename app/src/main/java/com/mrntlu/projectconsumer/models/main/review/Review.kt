package com.mrntlu.projectconsumer.models.main.review

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val author: Author,
    val star: Int,
    val review: String?,
    val popularity: Int,
    val likes: ArrayList<String>,

    @SerializedName("is_author")
    val isAuthor: Boolean,

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
): Parcelable, DiffUtilComparison<Review> {
    constructor(): this(
        Author(), 0, null, 0, arrayListOf(), false, "",  "",
        "", null, null, "", "", ""
    )

    override fun areItemsTheSame(newItem: Review): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: Review): Boolean {
        return when {
            id != newItem.id -> false
            star != newItem.star -> false
            review != newItem.review -> false
            popularity != newItem.popularity -> false
            contentID != newItem.contentID -> false
            likes.toSet() != newItem.likes.toSet() -> false
            contentExternalID != newItem.contentExternalID -> false
            contentExternalIntID != newItem.contentExternalIntID -> false
            else -> true
        }
    }
}

