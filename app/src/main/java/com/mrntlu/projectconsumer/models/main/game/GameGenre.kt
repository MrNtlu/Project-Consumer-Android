package com.mrntlu.projectconsumer.models.main.game

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameGenre(
    val name: String,

    @SerializedName("rawg_id")
    val rawgID: Int,
) : Parcelable
