package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListModel

data class GamePlayList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("game_id")
    override val contentId: String,

    @SerializedName("game_rawg_id")
    override val contentExternalId: String,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("hours_played")
    override val mainAttribute: Int?,

    @SerializedName("status")
    override val contentStatus: String,

    override val score: Int?,

    override val subAttribute: Int? = null,
): UserListModel()
