package com.mrntlu.projectconsumer.models.main.review

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    val image: String,
    val username: String,
    val email: String,

    @SerializedName("_id")
    val id: String,

    @SerializedName("is_premium")
    val isPremium: Boolean,
): Parcelable, DiffUtilComparison<Author> {
    constructor(): this("", "", "",  "", false)

    override fun areItemsTheSame(newItem: Author): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: Author): Boolean {
        return when {
            id != newItem.id -> false
            image != newItem.image -> false
            else -> true
        }
    }
}