package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListModel

data class AnimeWatchList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("anime_id")
    override val contentId: String,

    @SerializedName("anime_mal_id")
    override val contentExternalId: String,

    @SerializedName("watched_episodes")
    override val mainAttribute: Int,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("status")
    override val contentStatus: String,

    override val score: Int?,

    override val subAttribute: Int? = null,
): UserListModel()
