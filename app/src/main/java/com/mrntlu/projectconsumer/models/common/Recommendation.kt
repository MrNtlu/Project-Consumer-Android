package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recommendation(
    val description: String,

    @SerializedName("tmdb_id")
    val tmdbID: String,

    @SerializedName("title_en")
    val title: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("image_url")
    val imageURL: String,
) : Parcelable
