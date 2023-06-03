package com.mrntlu.projectconsumer.models.main.tv.entity

import androidx.room.ColumnInfo

data class NetworkEntity(
    val logo: String?,
    val name: String,

    @ColumnInfo("origin_country")
    val originCountry: String?
) {
    constructor(): this("", "", "")
}