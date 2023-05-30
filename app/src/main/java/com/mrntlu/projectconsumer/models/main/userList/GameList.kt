package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class GameList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("game_id")
    val gameId: String,

    @SerializedName("game_rawg_id")
    val gameRawgId: String,

    @SerializedName("achievement_status")
    val achievementStatus: Float?,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("hours_played")
    val hoursPlayer: Int?,

    val score: Int?,
    val status: String
)
