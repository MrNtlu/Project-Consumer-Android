package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class GameList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("content_status")
    val contentStatus: String,

    val score: Int?,

    @SerializedName("achievement_status")
    val achievementStatus: Float?,

    @SerializedName("hours_played")
    val hoursPlayed: Int?,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("game_id")
    val gameId: String,

    @SerializedName("rawg_id")
    val gameRawgId: String,

    val status: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageUrl: String?,

    val tba: Boolean,
)
