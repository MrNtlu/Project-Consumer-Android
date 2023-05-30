package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Translation(
    @SerializedName("lan_code")
    val lanCode: String,
    @SerializedName("lan_name")
    val lanName: String,
    @SerializedName("lan_name_en")
    val lanNameEn: String,
    val title: String,
    val description: String,
) : Parcelable {
    constructor(): this("","","","","")
}
