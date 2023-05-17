package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.movie.retrofit.MovieDetailsResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val repository: UserListRepository,
    private val userInteractionRepository: UserInteractionRepository,
    private val movieRepository: MovieRepository,
): ViewModel() {

    private val _movieWatchList = MutableLiveData<NetworkResponse<DataResponse<MovieWatchList>>>()
    val movieWatchList: LiveData<NetworkResponse<DataResponse<MovieWatchList>>> = _movieWatchList

    private val _consumeLater = MutableLiveData<NetworkResponse<DataResponse<ConsumeLater>>>()
    val consumeLater: LiveData<NetworkResponse<DataResponse<ConsumeLater>>> = _consumeLater

    private val _movieDetails = MutableLiveData<NetworkResponse<MovieDetailsResponse>>()
    val movieDetails: LiveData<NetworkResponse<MovieDetailsResponse>> = _movieDetails

    fun createMovieWatchList(body: MovieWatchListBody) = viewModelScope.launch(Dispatchers.IO) {
        repository.createMovieWatchList(body).collect { response ->
            withContext(Dispatchers.Main) {
                _movieWatchList.value = response
            }
        }
    }

    fun updateMovieWatchList(body: UpdateMovieWatchListBody) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateMovieWatchList(body).collect { response ->
            withContext(Dispatchers.Main) {
                _movieWatchList.value = response
            }
        }
    }

    fun createConsumeLater(body: ConsumeLaterBody) = viewModelScope.launch(Dispatchers.IO) {
        userInteractionRepository.createConsumeLater(body).collect { response ->
            withContext(Dispatchers.Main) {
                _consumeLater.value = response
            }
        }
    }

    fun deleteConsumeLater(body: IDBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.deleteConsumeLater(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun getMovieDetails(id: String) = viewModelScope.launch(Dispatchers.IO) {
        movieRepository.getMovieDetails(id).collect { response ->
            withContext(Dispatchers.Main) {
                _movieDetails.value = response
            }
        }
    }
}