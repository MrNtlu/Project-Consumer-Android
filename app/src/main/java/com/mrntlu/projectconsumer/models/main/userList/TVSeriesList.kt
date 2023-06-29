package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListContentModel

data class TVSeriesList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("content_status")
    override val contentStatus: String,

    override val score: Int?,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("watched_episodes")
    override val mainAttribute: Int?,

    @SerializedName("watched_seasons")
    override val watchedSeasons: Int?,

    @SerializedName("tv_id")
    override val contentId: String,

    @SerializedName("tmdb_id")
    override val contentExternalId: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("image_url")
    override val imageUrl: String?,

    @SerializedName("total_episodes")
    override val totalEpisodes: Int?,

    @SerializedName("total_seasons")
    override val totalSeasons: Int?,
): UserListContentModel()

fun UserListContentModel.convertToTVSeriesList(): TVSeriesList {
    return this.run {
        TVSeriesList(
            id, contentStatus, score, timesFinished, mainAttribute,
            watchedSeasons, contentId, contentExternalId, title, titleOriginal,
            imageUrl, totalEpisodes, totalSeasons,
        )
    }
}