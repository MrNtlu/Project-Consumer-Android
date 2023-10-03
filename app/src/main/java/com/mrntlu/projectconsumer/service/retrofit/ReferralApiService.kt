package com.mrntlu.projectconsumer.service.retrofit

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReferralApiService {
    @GET("api.php")
    suspend fun makeReferralRequest(
        @Query("ref") referralUrl: String,
    ): Response<String>
}