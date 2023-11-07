package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.Preview
import retrofit2.Response
import retrofit2.http.GET

interface PreviewApiService {

    @GET("preview")
    suspend fun getPreview(): Response<Preview>
}