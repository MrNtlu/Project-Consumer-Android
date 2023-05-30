package com.mrntlu.projectconsumer.models.common.entity

import androidx.room.ColumnInfo

data class TmdbGenreEntity(
    val name: String,
    @ColumnInfo("tmdb_id")
    val tmdbID: Int,
) {
    constructor(): this("", 0)
}
