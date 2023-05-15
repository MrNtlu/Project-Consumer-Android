package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.main.movie.retrofit.MovieDetailsResponse
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchListBody
import com.mrntlu.projectconsumer.repository.MovieRepository
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
    private val movieRepository: MovieRepository,
): ViewModel() {

    private val _movieWatchList = MutableLiveData<NetworkResponse<MovieWatchList>>()
    val movieWatchList: LiveData<NetworkResponse<MovieWatchList>> = _movieWatchList

    private val _movieDetails = MutableLiveData<NetworkResponse<MovieDetailsResponse>>()
    val movieDetails: LiveData<NetworkResponse<MovieDetailsResponse>> = _movieDetails

    fun createMovieWatchList(body: MovieWatchListBody) = viewModelScope.launch(Dispatchers.IO) {
        repository.createMovieWatchList(body).collect { response ->
            withContext(Dispatchers.Main) {
                _movieWatchList.value = response
            }
        }
    }

    fun getMovieDetails(id: String) = viewModelScope.launch(Dispatchers.IO) {
        movieRepository.getMovieDetails(id).collect { response ->
            withContext(Dispatchers.Main) {
                _movieDetails.value = response
            }
        }
    }
}