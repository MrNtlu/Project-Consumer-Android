package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName

data class Anime(
    @SerializedName("_id")
    val id: String,
) {
    constructor(): this("")
}
