package com.mrntlu.projectconsumer.models.main.game

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameRelation(
    val name: String,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("rawg_id")
    val rawgID: Int,
) : Parcelable
