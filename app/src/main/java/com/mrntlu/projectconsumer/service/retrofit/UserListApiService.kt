package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.main.userlist.MovieWatchListBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserListApiService {
    @POST("list/movie")
    suspend fun createMovieWatchList(@Body body: MovieWatchListBody): Response<String>
}