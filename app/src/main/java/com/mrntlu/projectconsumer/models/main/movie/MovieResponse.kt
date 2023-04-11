package com.mrntlu.projectconsumer.models.main.movie

import com.mrntlu.projectconsumer.models.common.PaginationResponse

data class MovieResponse(
    val data: List<Movie>,
    val pagination: PaginationResponse
)