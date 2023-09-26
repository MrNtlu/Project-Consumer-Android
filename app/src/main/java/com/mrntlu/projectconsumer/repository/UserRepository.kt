package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateFCMTokenBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
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

    fun updateUserImage(body: UpdateUserImageBody) = networkResponseFlow {
        userApiService.updateUserImage(body)
    }

    fun updateFCMToken(body: UpdateFCMTokenBody) = networkResponseFlow {
        userApiService.updateFCMToken(body)
    }

    fun updateMembership(body: UpdateMembershipBody) = networkResponseFlow {
        userApiService.updateMembership(body)
    }

    fun deleteUser() = networkResponseFlow {
        userApiService.deleteUser()
    }
}