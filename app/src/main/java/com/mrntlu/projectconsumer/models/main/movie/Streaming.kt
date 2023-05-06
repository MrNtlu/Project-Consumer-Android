package com.mrntlu.projectconsumer.models.main.movie

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Streaming(
    @SerializedName("buy_options")
    val buyOptions: List<StreamingPlatform>?,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("rent_options")
    val rentOptions: List<StreamingPlatform>?,
    @SerializedName("streaming_platforms")
    val streamingPlatforms: List<StreamingPlatform>?
) : Parcelable {
    constructor(): this(listOf<StreamingPlatform>(), "", listOf<StreamingPlatform>(), listOf<StreamingPlatform>())
}