package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeRelation(
    val relation: String,
    val source: List<AnimeRelationDetails>?,
) : Parcelable

@Parcelize
data class AnimeRelationDetails(
    val name: String,
    val type: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("redirect_url")
    val redirectURL: Int,
) : Parcelable