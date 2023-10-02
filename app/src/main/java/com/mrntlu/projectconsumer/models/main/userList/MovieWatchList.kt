package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListModel

data class MovieWatchList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("movie_id")
    override val contentId: String,

    @SerializedName("movie_tmdb_id")
    override val contentExternalId: String,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("status")
    override val contentStatus: String,

    @SerializedName("created_at")
    override val createdAt: String,

    override val score: Int?,

    override val mainAttribute: Int? = null,
    override val subAttribute: Int? = null,
): UserListModel()