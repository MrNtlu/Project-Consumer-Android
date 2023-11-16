package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.auth.retrofit.AnswerFriendRequestBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateFCMTokenBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateNotification
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdatePassword
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUsernameBody
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

    fun getUserProfile(username: String) = networkResponseFlow {
        userApiService.getUserProfile(username)
    }

    fun getFriendRequests() = networkResponseFlow {
        userApiService.getFriendRequests()
    }

    fun getFriends() = networkResponseFlow {
        userApiService.getFriends()
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

    fun changeUsername(body: UpdateUsernameBody) = networkResponseFlow {
        userApiService.changeUsername(body)
    }

    fun changePassword(body: UpdatePassword) = networkResponseFlow {
        userApiService.changePassword(body)
    }

    fun changeNotification(body: UpdateNotification) = networkResponseFlow {
        userApiService.changeNotification(body)
    }

    fun sendFriendRequest(body: UpdateUsernameBody) = networkResponseFlow {
        userApiService.sendFriendRequest(body)
    }

    fun answerFriendRequest(body: AnswerFriendRequestBody) = networkResponseFlow {
        userApiService.answerFriendRequest(body)
    }

    fun deleteUser() = networkResponseFlow {
        userApiService.deleteUser()
    }
}