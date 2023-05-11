package com.mrntlu.projectconsumer.models.main.userlist

import com.google.gson.annotations.SerializedName

data class MovieWatchListBody(
    @SerializedName("movie_id")
    val movieId: String,

    @SerializedName("movie_tmdb_id")
    val movieTMDBId: String,

    val score: Int?,
    val status: String
)
