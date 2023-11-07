package com.mrntlu.projectconsumer.models.common.retrofit

import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.tv.TVSeries

data class PreviewResponse<T>(
    val upcoming: List<T>,
    val popular: List<T>,
    val top: List<T>,
    val extra: List<T>?,
)

data class Preview(
    val movie: PreviewResponse<Movie>,
    val tv: PreviewResponse<TVSeries>,
    val anime: PreviewResponse<Anime>,
    val game: PreviewResponse<Game>,
)