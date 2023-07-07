package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
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