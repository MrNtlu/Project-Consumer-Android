package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class AnimeWatchList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("anime_id")
    val animeID: String,

    @SerializedName("anime_mal_id")
    val animeMalID: String,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int,

    @SerializedName("times_finished")
    val timesFinished: Int,

    val score: Int?,
    val status: String,
)
