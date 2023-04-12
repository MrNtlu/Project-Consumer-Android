package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName

data class ProductionAndCompany(
    val logo: String,
    val name: String,
    @SerializedName("origin_country")
    val originCountry: String
) {
    constructor(): this("", "", "")
}