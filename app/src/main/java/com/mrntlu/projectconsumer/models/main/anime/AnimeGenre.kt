package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeGenre(
    val name: String,
    val url: String,
) : Parcelable
