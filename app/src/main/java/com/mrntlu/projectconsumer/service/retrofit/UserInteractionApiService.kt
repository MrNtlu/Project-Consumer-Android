package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataNullableResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface UserInteractionApiService {
    @POST("consume")
    suspend fun createConsumeLater(@Body body: ConsumeLaterBody): Response<DataResponse<ConsumeLater>>

    @GET("consume")
    suspend fun getConsumeLater(
        @Query("type") type: String?,
        @Query("sort") sort: String,
    ): Response<DataNullableResponse<List<ConsumeLaterResponse>>>

    @HTTP(method = "DELETE", path = "consume", hasBody = true)
    suspend fun deleteConsumeLater(@Body body: IDBody): Response<MessageResponse>

    @DELETE("consume/all")
    suspend fun deleteAllConsumeLater(): Response<MessageResponse>
}