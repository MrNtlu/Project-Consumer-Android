package com.mrntlu.projectconsumer.models.common.retrofit

import com.google.gson.annotations.SerializedName

data class DayOfWeekResponse<T>(
    val data: List<T>,

    @SerializedName("day_of_week")
    val dayOfWeek: Int,
)
