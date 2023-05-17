package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserListApiService {
    @POST("list/movie")
    suspend fun createMovieWatchList(@Body body: MovieWatchListBody): Response<DataResponse<MovieWatchList>>

    @PATCH("list/movie")
    suspend fun updateMovieWatchList(@Body body: UpdateMovieWatchListBody): Response<DataResponse<MovieWatchList>>
}