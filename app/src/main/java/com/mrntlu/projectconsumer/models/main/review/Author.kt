package com.mrntlu.projectconsumer.models.main.review

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
): Parcelable {
    constructor(): this("", "", "",  "", false)
}