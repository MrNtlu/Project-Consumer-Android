package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class MovieWatchList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("movie_id")
    val movieId: String,

    @SerializedName("times_finished")
    val timesFinished: Int,

    val score: Int?,
    val status: String
)
