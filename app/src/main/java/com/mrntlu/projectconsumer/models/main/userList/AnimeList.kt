package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListContentModel

data class AnimeList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("content_status")
    override val contentStatus: String,

    override val score: Int?,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("watched_episodes")
    override val mainAttribute: Int,

    @SerializedName("anime_id")
    override val contentId: String,

    @SerializedName("mal_id")
    override val contentExternalId: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("image_url")
    override val imageUrl: String?,

    @SerializedName("total_episodes")
    override val totalEpisodes: Int?,

    override val totalSeasons: Int? = null,
    override val watchedSeasons: Int? = null,
): UserListContentModel()

fun UserListContentModel.convertToAnimeList(): AnimeList {
    return this.run {
        AnimeList(
            id, contentStatus, score, timesFinished,
            mainAttribute!!, contentId, contentExternalId, title,
            titleOriginal, imageUrl, totalEpisodes,
        )
    }
}