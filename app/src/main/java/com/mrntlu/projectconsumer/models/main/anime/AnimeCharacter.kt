package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName

data class AnimeCharacter(
    val name: String,
    val role: String,
    val image: String,

    @SerializedName("mal_id")
    val malID: Int,
)
