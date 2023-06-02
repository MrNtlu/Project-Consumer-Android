package com.mrntlu.projectconsumer.models.main.tv

import com.google.gson.annotations.SerializedName

data class Network(
    val logo: String,
    val name: String,

    @SerializedName("origin_country")
    val originCountry: String?
) {
    constructor(): this("", "", "")
}