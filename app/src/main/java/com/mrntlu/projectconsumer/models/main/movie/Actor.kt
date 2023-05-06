package com.mrntlu.projectconsumer.models.main.movie

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actor(
    val name: String,
    val image: String,
    val character: String,
) : Parcelable {
    constructor(): this("", "", "")
}
