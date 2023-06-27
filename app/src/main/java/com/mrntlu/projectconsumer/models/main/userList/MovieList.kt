package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class MovieList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("content_status")
    val contentStatus: String,

    val score: Int?,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("movie_id")
    val movieId: String,

    @SerializedName("tmdb_id")
    val movieTmdbId: String,

    val status: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageUrl: String?,
)
