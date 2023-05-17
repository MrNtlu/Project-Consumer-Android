package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class MovieWatchListBody(
    @SerializedName("movie_id")
    val movieId: String,

    @SerializedName("movie_tmdb_id")
    val movieTMDBId: String,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    val score: Int?,
    val status: String
)
