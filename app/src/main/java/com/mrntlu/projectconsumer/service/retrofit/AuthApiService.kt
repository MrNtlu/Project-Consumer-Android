package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.auth.retrofit.ForgotPasswordBody
import com.mrntlu.projectconsumer.models.auth.retrofit.GoogleLoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginResponse
import com.mrntlu.projectconsumer.models.auth.retrofit.RegisterBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterBody): Response<MessageResponse>

    @POST("oauth/google")
    suspend fun googleLogin(@Body body: GoogleLoginBody): Response<LoginResponse>

    @GET("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): Response<LoginResponse>

    @POST("user/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordBody): Response<MessageResponse>
}