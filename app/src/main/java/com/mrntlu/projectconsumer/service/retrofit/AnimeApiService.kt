package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AnimeApiService {

    @GET("anime/details")
    suspend fun getAnimeDetails(
        @Query("id") id: String,
    ): Response<DataResponse<AnimeDetails>>
}