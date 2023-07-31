package com.mrntlu.projectconsumer.models.main.game

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameMetacriticScorePlatform(
    val score: Float,
    val platform: String,
) : Parcelable
