package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.main.movie.MovieResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie")
    suspend fun getMovieBySortFilter(
        @Query("page") page: Int,
        @Query("sort") sort: String,
        @Query("status") status: String?,
        @Query("production_companies") productionCompanies: String?,
        @Query("genres") genres: String?,
        @Query("from") releaseDateFrom: Int?,
        @Query("to") releaseDateTo: Int?,
    ): Response<MovieResponse>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<MovieResponse>

    @GET("movie/decade")
    suspend fun getPopularMoviesByDecade(
        @Query("page") page: Int,
    )

    @GET("movie/genre")
    suspend fun getPopularMoviesByGenre(
        @Query("page") page: Int,
    )
}