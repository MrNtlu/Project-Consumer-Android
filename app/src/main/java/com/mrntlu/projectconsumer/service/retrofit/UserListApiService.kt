package com.mrntlu.projectconsumer.service.retrofit

import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.AnimeWatchList
import com.mrntlu.projectconsumer.models.main.userList.GamePlayList
import com.mrntlu.projectconsumer.models.main.userList.LogsByDate
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.AnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.GamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.IncrementTVSeriesListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateAnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateGamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateTVWatchListBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface UserListApiService {
    @GET("list")
    suspend fun getUserList(@Query("sort") sort: String): Response<DataResponse<UserList>>

    @GET("list/logs")
    suspend fun getUserLogs(
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<DataResponse<List<LogsByDate>?>>

    @POST("list/movie")
    suspend fun createMovieWatchList(@Body body: MovieWatchListBody): Response<DataResponse<MovieWatchList>>

    @PATCH("list/movie")
    suspend fun updateMovieWatchList(@Body body: UpdateMovieWatchListBody): Response<DataResponse<MovieWatchList>>

    @POST("list/tv")
    suspend fun createTVWatchList(@Body body: TVWatchListBody): Response<DataResponse<TVSeriesWatchList>>

    @PATCH("list/tv")
    suspend fun updateTVWatchList(@Body body: UpdateTVWatchListBody): Response<DataResponse<TVSeriesWatchList>>

    @PATCH("list/tv/inc")
    suspend fun incrementTVWatchList(@Body body: IncrementTVSeriesListBody): Response<DataResponse<TVSeriesWatchList>>

    @POST("list/anime")
    suspend fun createAnimeWatchList(@Body body: AnimeWatchListBody): Response<DataResponse<AnimeWatchList>>

    @PATCH("list/anime")
    suspend fun updateAnimeWatchList(@Body body: UpdateAnimeWatchListBody): Response<DataResponse<AnimeWatchList>>

    @PATCH("list/anime/inc")
    suspend fun incrementAnimeWatchList(@Body body: IDBody): Response<DataResponse<AnimeWatchList>>

    @POST("list/game")
    suspend fun createGamePlayList(@Body body: GamePlayListBody): Response<DataResponse<GamePlayList>>

    @PATCH("list/game")
    suspend fun updateGamePlayList(@Body body: UpdateGamePlayListBody): Response<DataResponse<GamePlayList>>

    @PATCH("list/game/inc")
    suspend fun incrementGamePlayList(@Body body: IDBody): Response<DataResponse<GamePlayList>>

    @HTTP(method = "DELETE", path = "list", hasBody = true)
    suspend fun deleteUserList(@Body body: DeleteUserListBody): Response<MessageResponse>
}