package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.UserApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
) {

    fun getBasicUserInfo() = networkResponseFlow {
        userApiService.getBasicUserInfo()
    }

    fun getUserInfo() = networkResponseFlow {
        userApiService.getUserInfo()
    }
}