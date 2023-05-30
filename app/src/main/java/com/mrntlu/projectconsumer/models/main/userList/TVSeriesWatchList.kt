package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class TVSeriesWatchList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("tv_id")
    val tvId: String,

    @SerializedName("tv_tmdb_id")
    val tvTmdbId: String,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int,

    @SerializedName("watched_seasons")
    val watchedSeasons: Int,

    val score: Int?,
    val status: String
)
