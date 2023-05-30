package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName

data class Streaming(
    @SerializedName("buy_options")
    val buyOptions: List<StreamingPlatform>?,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("rent_options")
    val rentOptions: List<StreamingPlatform>?,
    @SerializedName("streaming_platforms")
    val streamingPlatforms: List<StreamingPlatform>?
) {
    constructor(): this(listOf<StreamingPlatform>(), "", listOf<StreamingPlatform>(), listOf<StreamingPlatform>())
}