package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.auth.retrofit.GoogleLoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.RegisterBody
import com.mrntlu.projectconsumer.service.retrofit.AuthApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
) {

    fun login(body: LoginBody) = networkResponseFlow {
        authApiService.login(body)
    }

    fun register(body: RegisterBody) = networkResponseFlow {
        authApiService.register(body)
    }

    fun googleLogin(body: GoogleLoginBody) = networkResponseFlow {
        authApiService.googleLogin(body)
    }
}