package com.mrntlu.projectconsumer.service

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2ODQzMzY5MzAsImlkIjoiNjQxZGFlZTNjYTQ4MzdjZTRlZjVmY2M5Iiwib3JpZ19pYXQiOjE2ODQwNzc3MzB9.tZ0xtSyWRrcJprw_iEqPE1d28kxIW5zWIsmaVxfI29s")
        return chain.proceed(request.build())
    }
}