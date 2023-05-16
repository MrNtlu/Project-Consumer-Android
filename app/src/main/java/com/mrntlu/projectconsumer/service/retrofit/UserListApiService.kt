package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.main.userList.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserListApiService {
    @POST("list/movie")
    suspend fun createMovieWatchList(@Body body: MovieWatchListBody): Response<MovieWatchListResponse>

    @PATCH("list/movie")
    suspend fun updateMovieWatchList(@Body body: UpdateMovieWatchListBody): Response<MovieWatchListResponse>
}