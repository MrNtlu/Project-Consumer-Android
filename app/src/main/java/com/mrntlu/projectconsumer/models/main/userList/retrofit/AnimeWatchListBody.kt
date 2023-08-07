package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class AnimeWatchListBody(
    @SerializedName("anime_id")
    val animeId: String,

    @SerializedName("anime_mal_id")
    val animeMalId: Int,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int?,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    val score: Int?,
    val status: String,
)
