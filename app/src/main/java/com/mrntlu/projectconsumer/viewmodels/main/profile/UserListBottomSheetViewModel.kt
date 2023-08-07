package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.AnimeWatchList
import com.mrntlu.projectconsumer.models.main.userList.GamePlayList
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.AnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.GamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateAnimeWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateGamePlayListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateTVWatchListBody
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserListBottomSheetViewModel @Inject constructor(
    private val repository: UserListRepository,
): ViewModel() {
    private val _tvWatchList = MutableLiveData<NetworkResponse<DataResponse<TVSeriesWatchList>>>()
    val tvWatchList: LiveData<NetworkResponse<DataResponse<TVSeriesWatchList>>> = _tvWatchList

    private val _movieWatchList = MutableLiveData<NetworkResponse<DataResponse<MovieWatchList>>>()
    val movieWatchList: LiveData<NetworkResponse<DataResponse<MovieWatchList>>> = _movieWatchList

    private val _animeWatchList = MutableLiveData<NetworkResponse<DataResponse<AnimeWatchList>>>()
    val animeWatchList: LiveData<NetworkResponse<DataResponse<AnimeWatchList>>> = _animeWatchList

    private val _gamePlayList = MutableLiveData<NetworkResponse<DataResponse<GamePlayList>>>()
    val gamePlayList: LiveData<NetworkResponse<DataResponse<GamePlayList>>> = _gamePlayList

    fun createTVWatchList(body: TVWatchListBody) = networkResponseFlowCollector(
        repository.createTVWatchList(body)
    ) { response ->
        _tvWatchList.value = response
    }

    fun updateTVWatchList(body: UpdateTVWatchListBody) = networkResponseFlowCollector(
        repository.updateTVWatchList(body)
    ) { response ->
        _tvWatchList.value = response
    }

    fun createMovieWatchList(body: MovieWatchListBody) = networkResponseFlowCollector(
        repository.createMovieWatchList(body)
    ) { response ->
        _movieWatchList.value = response
    }

    fun updateMovieWatchList(body: UpdateMovieWatchListBody) = networkResponseFlowCollector(
        repository.updateMovieWatchList(body)
    ) { response ->
        _movieWatchList.value = response
    }

    fun createAnimeWatchList(body: AnimeWatchListBody) = networkResponseFlowCollector(
        repository.createAnimeWatchList(body)
    ) { response ->
        _animeWatchList.value = response
    }

    fun updateAnimeWatchList(body: UpdateAnimeWatchListBody) = networkResponseFlowCollector(
        repository.updateAnimeWatchList(body)
    ) { response ->
        _animeWatchList.value = response
    }

    fun createGamePlayList(body: GamePlayListBody) = networkResponseFlowCollector(
        repository.createGamePlayList(body)
    ) { response ->
        _gamePlayList.value = response
    }

    fun updateGamePlayList(body: UpdateGamePlayListBody) = networkResponseFlowCollector(
        repository.updateGamePlayList(body)
    ) { response ->
        _gamePlayList.value = response
    }

    fun deleteUserList(body: DeleteUserListBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserList(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}