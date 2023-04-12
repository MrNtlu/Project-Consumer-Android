package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.ColumnInfo

data class MovieGenreEntity(
    val name: String,
    @ColumnInfo("tmdb_id")
    val tmdbID: Int,
) {
    constructor(): this("", 0)
}
