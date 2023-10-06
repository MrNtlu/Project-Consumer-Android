package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class IncrementTVSeriesListBody(
    val id: String,

    @SerializedName("is_episode")
    val isEpisode: Boolean,
)
