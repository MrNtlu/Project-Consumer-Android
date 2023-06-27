package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class TVSeriesList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("content_status")
    val contentStatus: String,

    val score: Int?,

    @SerializedName("watched_episodes")
    val watchedEpisodes: Int,

    @SerializedName("watched_seasons")
    val watchedSeasons: Int,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("tv_id")
    val tvId: String,

    @SerializedName("tmdb_id")
    val tvTmdbId: String,

    val status: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("total_episodes")
    val totalEpisodes: Int?,

    @SerializedName("total_seasons")
    val totalSeasons: Int?,
)
