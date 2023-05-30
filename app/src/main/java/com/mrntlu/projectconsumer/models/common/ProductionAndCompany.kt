package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductionAndCompany(
    val logo: String?,
    val name: String,
    @SerializedName("origin_country")
    val originCountry: String
) : Parcelable {
    constructor(): this("", "", "")
}