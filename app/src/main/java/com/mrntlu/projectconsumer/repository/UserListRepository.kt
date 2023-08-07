package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.main.userList.retrofit.AnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.GamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateAnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateGamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateTVWatchListBody
import com.mrntlu.projectconsumer.service.retrofit.UserListApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserListRepository @Inject constructor(
    private val userListApiService: UserListApiService,
) {
    fun getUserList(sort: String) = networkResponseFlow {
        userListApiService.getUserList(sort)
    }

    fun getUserLogs(from: String, to: String) = networkResponseFlow {
        userListApiService.getUserLogs(from, to)
    }

    fun createMovieWatchList(body: MovieWatchListBody) = networkResponseFlow {
        userListApiService.createMovieWatchList(body)
    }

    fun updateMovieWatchList(body: UpdateMovieWatchListBody) = networkResponseFlow {
        userListApiService.updateMovieWatchList(body)
    }

    fun createTVWatchList(body: TVWatchListBody) = networkResponseFlow {
        userListApiService.createTVWatchList(body)
    }

    fun updateTVWatchList(body: UpdateTVWatchListBody) = networkResponseFlow {
        userListApiService.updateTVWatchList(body)
    }

    fun createAnimeWatchList(body: AnimeWatchListBody) = networkResponseFlow {
        userListApiService.createAnimeWatchList(body)
    }

    fun updateAnimeWatchList(body: UpdateAnimeWatchListBody) = networkResponseFlow {
        userListApiService.updateAnimeWatchList(body)
    }

    fun createGamePlayList(body: GamePlayListBody) = networkResponseFlow {
        userListApiService.createGamePlayList(body)
    }

    fun updateGamePlayList(body: UpdateGamePlayListBody) = networkResponseFlow {
        userListApiService.updateGamePlayList(body)
    }

    fun deleteUserList(body: DeleteUserListBody) = networkResponseFlow {
        userListApiService.deleteUserList(body)
    }
}