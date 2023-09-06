package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate

data class UserInfoCommon(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("times_finished")
    val timesFinished: Int,

    @SerializedName("content_type")
    val contentType: String,

    override val description: String = "",
    override val score: Float = 0f,
    override val releaseDate: String? = null,
    override val episodes: Int? = null,
    override val totalSeasons: Int? = null,
    override val length: Int? = null,
    override val aired: AnimeAirDate? = null,
): ContentModel()
