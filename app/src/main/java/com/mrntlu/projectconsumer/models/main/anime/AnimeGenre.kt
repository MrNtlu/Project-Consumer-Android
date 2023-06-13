package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName

data class AnimeGenre(
    val name: String,
    val url: String,

    @SerializedName("mal_id")
    val malID: Int,
)
