package com.mrntlu.projectconsumer.service.retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
    )

    @GET("movie/decade")
    suspend fun getPopularMoviesByDecade(
        @Query("page") page: Int,
    )

    @GET("movie/genre")
    suspend fun getPopularMoviesByGenre(
        @Query("page") page: Int,
    )
}