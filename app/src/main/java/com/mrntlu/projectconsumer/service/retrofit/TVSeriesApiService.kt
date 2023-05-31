package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataSearchPaginationResponse
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TVSeriesApiService {
    @GET("tv")
    suspend fun getTVSeriesBySortFilter(
        @Query("page") page: Int,
        @Query("sort") sort: String,
        @Query("status") status: String?,
        @Query("season") numOfSeason: String?,
        @Query("production_companies") productionCompanies: String?,
        @Query("genres") genres: String?,
        @Query("from") releaseDateFrom: Int?,
        @Query("to") releaseDateTo: Int?,
    ): Response<DataPaginationResponse<TVSeries>>

    @GET("tv/upcoming")
    suspend fun getUpcomingTVSeries(
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<DataPaginationResponse<TVSeries>>

    @GET("tv/search")
    suspend fun searchTVSeriesByTitle(
        @Query("search") search: String,
        @Query("page") page: Int,
    ): Response<DataSearchPaginationResponse<TVSeries>>

    @GET("tv/details")
    suspend fun getTVSeriesDetails(
        @Query("id") id: String,
    ): Response<DataResponse<TVSeriesDetails>>
}