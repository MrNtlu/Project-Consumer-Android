package com.mrntlu.projectconsumer.models.main.movie.retrofit

import com.mrntlu.projectconsumer.models.common.PaginationResponse
import com.mrntlu.projectconsumer.models.main.movie.Movie

data class MoviePaginationResponse(
    val data: List<Movie>,
    val pagination: PaginationResponse
)