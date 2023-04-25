package com.mrntlu.projectconsumer.service

import com.mrntlu.projectconsumer.models.auth.LoginResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        return runBlocking {
            val newToken = getNewToken(token)

            if (!newToken.isSuccessful || newToken.body() == null) {
                tokenManager.deleteToken()
            }

            newToken.body()?.let {
                tokenManager.saveToken(it.token)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${it.token}")
                    .build()
            }
        }
    }

    // TODO Check a way to pass retrofit via hilt
    // google://android authenticator hilt retrofit
    private suspend fun getNewToken(refreshToken: String?): retrofit2.Response<LoginResponse> {
        TODO("Uncomment")
//        val okHttpClient = OkHttpClient.Builder().build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(Constants.API_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//        val service = retrofit.create(AuthApiService::class.java)
//        return service.refreshToken("Bearer $refreshToken")
    }
}