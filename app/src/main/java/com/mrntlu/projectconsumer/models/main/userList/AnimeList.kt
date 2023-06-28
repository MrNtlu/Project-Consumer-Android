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
    override val watchedEpisodes: Int,

    override val watchedSeasons: Int? = null,

    @SerializedName("anime_id")
    override val contentId: String,

    @SerializedName("mal_id")
    override val contentExternalId: String,

    val status: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("image_url")
    override val imageUrl: String?,

    @SerializedName("total_episodes")
    override val totalEpisodes: Int?,

    val type: String,

    @SerializedName("is_airing")
    val isAiring: Boolean,
): UserListContentModel()
