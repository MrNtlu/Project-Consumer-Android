package com.mrntlu.projectconsumer.models.main.userList.retrofit

import com.google.gson.annotations.SerializedName

data class GamePlayListBody(
    @SerializedName("game_id")
    val gameId: String,

    @SerializedName("game_rawg_id")
    val gameRawgId: Int,

    @SerializedName("times_finished")
    val timesFinished: Int?,

    @SerializedName("hours_played")
    val hoursPlayed: Int?,

    val score: Int?,
    val status: String,
)
