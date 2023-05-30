package com.mrntlu.projectconsumer.models.common.entity

import androidx.room.ColumnInfo

data class TranslationEntity(
    @ColumnInfo("lan_code")
    val lanCode: String,
    @ColumnInfo("lan_name")
    val lanName: String,
    @ColumnInfo("lan_name_en")
    val lanNameEn: String,
    val title: String,
    val description: String,
) {
    constructor(): this("","","","","")
}
