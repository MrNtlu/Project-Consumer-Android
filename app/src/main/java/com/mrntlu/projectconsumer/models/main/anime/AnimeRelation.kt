package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName

data class AnimeRelation(
    val relation: String,
    val source: List<AnimeRelationDetails>?,
)

data class AnimeRelationDetails(
    val name: String,
    val type: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("redirect_url")
    val redirectURL: Int,
)