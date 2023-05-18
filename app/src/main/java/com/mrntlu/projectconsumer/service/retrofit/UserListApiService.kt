package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserListApiService {
    @POST("list/movie")
    suspend fun createMovieWatchList(@Body body: MovieWatchListBody): Response<DataResponse<MovieWatchList>>

    @PATCH("list/movie")
    suspend fun updateMovieWatchList(@Body body: UpdateMovieWatchListBody): Response<DataResponse<MovieWatchList>>

    @HTTP(method = "DELETE", path = "list", hasBody = true)
    suspend fun deleteUserList(@Body body: DeleteUserListBody): Response<MessageResponse>
}