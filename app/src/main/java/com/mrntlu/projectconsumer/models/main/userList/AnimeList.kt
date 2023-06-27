package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class AnimeList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("content_status")
    val contentStatus: String,

    val score: Int?,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int,

    @SerializedName("anime_id")
    val animeId: String,

    @SerializedName("mal_id")
    val animeMALId: String,

    val status: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("total_episodes")
    val totalEpisodes: Int?,

    val type: String,

    @SerializedName("is_airing")
    val isAiring: Boolean,
)
