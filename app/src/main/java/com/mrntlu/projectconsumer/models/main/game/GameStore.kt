package com.mrntlu.projectconsumer.models.main.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameStore(
    val url: String,
    val stores: Int,
) : Parcelable
