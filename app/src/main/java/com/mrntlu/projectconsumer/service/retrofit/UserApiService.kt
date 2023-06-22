package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserApiService {

    @GET("user/basic")
    suspend fun getBasicUserInfo(): Response<DataResponse<BasicUserInfo>>

    @GET("user/info")
    suspend fun getUserInfo(): Response<DataResponse<UserInfo>>
}