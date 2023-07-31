package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeCharacter(
    val name: String,
    val role: String,
    val image: String,

    @SerializedName("mal_id")
    val malID: Int,
) : Parcelable
