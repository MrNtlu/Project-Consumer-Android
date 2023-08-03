package com.mrntlu.projectconsumer.models.main.anime

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeAirDate(
    val from: String?,
    val to: String?,

    @SerializedName("from_day")
    val fromDay: Int?,

    @SerializedName("from_month")
    val fromMonth: Int?,

    @SerializedName("from_year")
    val fromYear: Int?,

    @SerializedName("to_day")
    val toDay: Int?,

    @SerializedName("to_month")
    val toMonth: Int?,

    @SerializedName("to_year")
    val toYear: Int?,
) : Parcelable {
    constructor(): this(
        "", "", 0, 0, 0, 0, 0, 0
    )
}
