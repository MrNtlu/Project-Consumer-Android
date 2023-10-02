package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeRelation(
    @SerializedName("_id")
    val id: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("anime_id")
    val animeID: String,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    val relation: String,
    val type: String,
) : Parcelable