package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataSearchPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimeApiService {

    @GET("anime")
    suspend fun getAnimesBySortFilter(
        @Query("page") page: Int,
        @Query("sort") sort: String,
        @Query("status") status: String?,
        @Query("genres") genres: String?,
        @Query("demographics") demographics: String?,
        @Query("themes") themes: String?,
        @Query("studios") studios: String?,
    ): Response<DataPaginationResponse<Anime>>

    @GET("anime/preview")
    suspend fun getPreviewAnimes(): Response<PreviewResponse<Anime>>

    @GET("anime/upcoming")
    suspend fun getUpcomingAnimes(
        @Query("page") page: Int,
    ): Response<DataPaginationResponse<Anime>>

    //TODO Implement
    @GET("anime/airing")
    suspend fun getCurrentlyAiringAnimesByDayOfWeek(): Response<DataResponse<Anime>>

    @GET("anime/search")
    suspend fun searchAnimesByTitle(
        @Query("search") search: String,
        @Query("page") page: Int,
    ): Response<DataSearchPaginationResponse<Anime>>

    @GET("anime/details")
    suspend fun getAnimeDetails(
        @Query("id") id: String,
    ): Response<DataResponse<AnimeDetails>>
}