package com.mrntlu.projectconsumer.models.main.userInteraction.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consume_laters")
data class ConsumeLaterEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @ColumnInfo("user_id")
    val userID: String,

    @ColumnInfo("content_id")
    val contentID: String,

    @ColumnInfo("content_external_id")
    val contentExternalID: String?,

    @ColumnInfo("content_external_int_id")
    val contentExternalIntID: Int?,

    @ColumnInfo("content_type")
    val contentType: String,

    @ColumnInfo("self_note")
    val selfNote: String?,

    @ColumnInfo("created_at")
    val createdAt: String,

    @Embedded
    val content: ConsumeLaterContentEntity,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "", "", "", null, null, "",
        null, "", ConsumeLaterContentEntity(), "", 0
    )
}

data class ConsumeLaterContentEntity(

    @ColumnInfo("title_original")
    val titleOriginal: String,

    @ColumnInfo("image_url")
    val imageURL: String,

    val description: String,
) {
    constructor(): this("", "", "")
}