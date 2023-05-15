package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.main.movie.retrofit.MovieDetailsResponse
import com.mrntlu.projectconsumer.models.main.movie.retrofit.MoviePaginationResponse
import com.mrntlu.projectconsumer.models.main.movie.retrofit.MovieResponse
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
    ): Response<MoviePaginationResponse>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<MoviePaginationResponse>

    @GET("movie/search")
    suspend fun searchMovies(
        @Query("search") search: String,
    ): Response<MovieResponse>

    @GET("movie/decade")
    suspend fun getPopularMoviesByDecade(
        @Query("page") page: Int,
    )

    @GET("movie/genre")
    suspend fun getPopularMoviesByGenre(
        @Query("page") page: Int,
    )

    @GET("movie/details")
    suspend fun getMovieDetails(
        @Query("id") id: String,
    ): Response<MovieDetailsResponse>
}