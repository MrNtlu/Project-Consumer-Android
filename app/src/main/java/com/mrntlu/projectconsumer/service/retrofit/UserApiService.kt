package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateFCMTokenBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {

    @GET("user/basic")
    suspend fun getBasicUserInfo(): Response<DataResponse<BasicUserInfo>>

    @GET("user/info")
    suspend fun getUserInfo(): Response<DataResponse<UserInfo>>

    @PATCH("user/image")
    suspend fun updateUserImage(@Body body: UpdateUserImageBody): Response<MessageResponse>

    @PATCH("user/token")
    suspend fun updateFCMToken(@Body body: UpdateFCMTokenBody): Response<MessageResponse>

    @PATCH("user/membership")
    suspend fun updateMembership(@Body body: UpdateMembershipBody): Response<MessageResponse>

    @DELETE("user/delete")
    suspend fun deleteUser(): Response<MessageResponse>
}