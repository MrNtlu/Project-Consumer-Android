package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class MoviePreviewRepository @Inject constructor(
    private val movieApiService: MovieApiService,
) {

    fun fetchUpcomingMovies() = networkResponseFlow {
        movieApiService.getUpcomingMovies(1, "popularity")
    }

    fun fetchPopularMovies() = networkResponseFlow {
        movieApiService.getMovieBySortFilter(
            1, "popularity",
            null, null, null, null, null,
        )
    }
}