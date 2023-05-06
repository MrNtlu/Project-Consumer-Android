package com.mrntlu.projectconsumer.models.main.movie

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StreamingPlatform(
    val logo: String,
    val name: String
) : Parcelable {
    constructor(): this("", "")
}