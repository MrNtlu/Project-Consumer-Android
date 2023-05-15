package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.main.userList.MovieWatchListBody
import com.mrntlu.projectconsumer.service.retrofit.UserListApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserListRepository @Inject constructor(
    private val userListApiService: UserListApiService,
) {

    fun createMovieWatchList(body: MovieWatchListBody) = networkResponseFlow {
        userListApiService.createMovieWatchList(body)
    }
}