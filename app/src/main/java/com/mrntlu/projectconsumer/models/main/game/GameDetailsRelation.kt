package com.mrntlu.projectconsumer.models.main.game

import com.google.gson.annotations.SerializedName

data class GameDetailsRelation(
    @SerializedName("_id")
    val id: String,
    val platforms: List<String>,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("rawg_id")
    val rawgID: Int,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("image_url")
    val imageURL: String,
)
