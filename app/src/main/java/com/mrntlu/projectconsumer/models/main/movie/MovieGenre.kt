package com.mrntlu.projectconsumer.models.main.movie

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieGenre(
    val name: String,
    @SerializedName("tmdb_id")
    val tmdbID: Int,
) : Parcelable {
    constructor(): this("", 0)
}