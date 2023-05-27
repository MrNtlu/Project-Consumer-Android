package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.auth.User
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {

    @GET("user/info")
    suspend fun getUserInfo(): Response<DataResponse<User>>
}