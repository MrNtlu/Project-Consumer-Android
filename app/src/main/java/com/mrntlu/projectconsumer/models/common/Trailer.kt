package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trailer(
    val name: String,
    val key: String,
    val type: String,
): Parcelable {
    constructor(): this("", "", "")
}
