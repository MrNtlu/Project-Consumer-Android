package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class AnimeList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("anime_id")
    val animeId: String,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int,

    @SerializedName("anime_mal_id")
    val animeMALId: String,

    @SerializedName("times_finished")
    val timesFinished: Int,

    val score: Int?,
    val status: String
)
