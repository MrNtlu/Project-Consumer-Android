package com.mrntlu.projectconsumer.models.main.tv

import com.google.gson.annotations.SerializedName

data class Season(
    val description: String,
    val name: String,

    @SerializedName("air_date")
    val airDate: String,

    @SerializedName("episode_count")
    val episodeCount: Int,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("season_num")
    val seasonNum: Int
) {
    constructor(): this("", "", "", 0, "", 0)
}