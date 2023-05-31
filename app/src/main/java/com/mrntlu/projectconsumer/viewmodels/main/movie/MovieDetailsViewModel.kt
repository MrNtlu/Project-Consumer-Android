package com.mrntlu.projectconsumer.viewmodels.main.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val repository: UserListRepository,
    private val movieRepository: MovieRepository,
): ViewModel() {

    private val _movieWatchList = MutableLiveData<NetworkResponse<DataResponse<MovieWatchList>>>()
    val movieWatchList: LiveData<NetworkResponse<DataResponse<MovieWatchList>>> = _movieWatchList

    private val _movieDetails = MutableLiveData<NetworkResponse<DataResponse<MovieDetails>>>()
    val movieDetails: LiveData<NetworkResponse<DataResponse<MovieDetails>>> = _movieDetails

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

    fun getMovieDetails(id: String) = networkResponseFlowCollector(
        movieRepository.getMovieDetails(id)
    ) { response ->
        _movieDetails.value = response
    }
}