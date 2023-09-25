package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.AISuggestionResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import retrofit2.Response
import retrofit2.http.GET

interface AISuggestionApiService {
    @GET("suggestions")
    suspend fun getAISuggestions(): Response<DataResponse<AISuggestionResponse>>
}