package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeRecommendation(
    val title: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("image_url")
    val imageURL: String,
) : Parcelable
