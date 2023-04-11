package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName

data class MovieGenre(
    val name: String,
    @SerializedName("tmdb_id")
    val tmdbID: Int,
)