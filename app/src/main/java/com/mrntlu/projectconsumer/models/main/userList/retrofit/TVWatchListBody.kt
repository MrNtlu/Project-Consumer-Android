package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class TVWatchListBody(
    @SerializedName("tv_id")
    val tvId: String,

    @SerializedName("tv_tmdb_id")
    val tvTMDBId: String,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int?,

    @SerializedName("watched_seasons")
    val watchedSeasons: Int?,

    val score: Int?,
    val status: String
)
