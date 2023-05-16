package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class UpdateMovieWatchListBody(
    val id: String,

    @SerializedName("is_updating_score")
    val isUpdatingScore: Boolean,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    val score: Int?,
    val status: String?,
)
