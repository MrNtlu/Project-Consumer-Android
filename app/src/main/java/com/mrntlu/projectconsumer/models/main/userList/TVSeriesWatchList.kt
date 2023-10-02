package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListModel

data class TVSeriesWatchList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("tv_id")
    override val contentId: String,

    @SerializedName("tv_tmdb_id")
    override val contentExternalId: String,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("watched_episodes")
    override val mainAttribute: Int,

    @SerializedName("watched_seasons")
    override val subAttribute: Int,

    @SerializedName("status")
    override val contentStatus: String,

    @SerializedName("created_at")
    override val createdAt: String,

    override val score: Int?,
): UserListModel()
