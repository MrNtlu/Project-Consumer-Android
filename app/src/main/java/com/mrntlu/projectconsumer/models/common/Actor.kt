package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actor(
    @SerializedName("tmdb_id")
    val tmdbID: String,

    val name: String,
    val image: String?,
    val character: String,
) : Parcelable {
    constructor(): this("", "", "", "")
}
