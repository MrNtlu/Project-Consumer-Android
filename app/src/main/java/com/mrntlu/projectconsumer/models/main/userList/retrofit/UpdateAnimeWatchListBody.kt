package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateAnimeWatchListBody(
    val id: String,

    @SerializedName("is_updating_score")
    val isUpdatingScore: Boolean,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int?,

    val score: Int?,
    val status: String?
)
