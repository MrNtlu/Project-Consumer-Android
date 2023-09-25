package com.mrntlu.projectconsumer.models.common

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import com.mrntlu.projectconsumer.models.main.game.GameDetails
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails

data class AISuggestionResponse(
    val suggestions: List<AISuggestion>,

    @SerializedName("created_at")
    val createdAt: String,
)
