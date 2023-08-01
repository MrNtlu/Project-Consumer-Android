package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.models.main.game.GameDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GameApiService {

    @GET("game")
    suspend fun getGamesBySortFilter(
        @Query("page") page: Int,
        @Query("sort") sort: String,
        @Query("tba") tba: Boolean?,
        @Query("genres") genres: String?,
    ): Response<DataPaginationResponse<Game>>

    @GET("game/preview")
    suspend fun getPreviewGames(): Response<PreviewResponse<Game>>

    @GET("game/upcoming")
    suspend fun getUpcomingGames(
        @Query("page") page: Int,
        @Query("sort") sort: String,
    ): Response<DataPaginationResponse<Game>>

    @GET("game/details")
    suspend fun getGameDetails(
        @Query("id") id: String,
    ): Response<DataResponse<GameDetails>>
}