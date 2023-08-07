package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateGamePlayListBody(
    val id: String,

    @SerializedName("is_updating_score")
    val isUpdatingScore: Boolean,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    @SerializedName("hours_played")
    val hoursPlayed: Int?,

    val score: Int?,
    val status: String?
)
