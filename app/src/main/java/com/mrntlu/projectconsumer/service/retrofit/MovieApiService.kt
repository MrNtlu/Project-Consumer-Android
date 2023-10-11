package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataSearchPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
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
    ): Response<DataPaginationResponse<Movie>>

    @GET("movie/preview")
    suspend fun getPreviewMovies(): Response<PreviewResponse<Movie>>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
    ): Response<DataPaginationResponse<Movie>>

    @GET("movie/theaters")
    suspend fun getMoviesInTheater(
        @Query("page") page: Int,
    ): Response<DataPaginationResponse<Movie>>

    @GET("movie/search")
    suspend fun searchMoviesByTitle(
        @Query("search") search: String,
        @Query("page") page: Int,
    ): Response<DataSearchPaginationResponse<Movie>>

    @GET("movie/details")
    suspend fun getMovieDetails(
        @Query("id") id: String,
    ): Response<DataResponse<MovieDetails>>
}