package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.main.movie.MovieResponse
import com.mrntlu.projectconsumer.repository.MoviePreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MoviePreviewViewModel @Inject constructor(
    private val moviePreviewRepository: MoviePreviewRepository,
): ViewModel() {
    private val _upcomingList = MutableLiveData<NetworkResponse<MovieResponse>>()
    val upcomingMovies: LiveData<NetworkResponse<MovieResponse>> = _upcomingList

    private val _popularList = MutableLiveData<NetworkResponse<MovieResponse>>()
    val popularMovies: LiveData<NetworkResponse<MovieResponse>> = _popularList

    init {
        fetchUpcomingMovies()
        fetchPopularMovies()
    }

    fun fetchUpcomingMovies() = viewModelScope.launch(Dispatchers.IO) {
        moviePreviewRepository.fetchUpcomingMovies().collect { response ->
            withContext(Dispatchers.Main) {
                _upcomingList.value = response
            }
        }
    }

    fun fetchPopularMovies() = viewModelScope.launch(Dispatchers.IO) {
        moviePreviewRepository.fetchPopularMovies().collect { response ->
            withContext(Dispatchers.Main) {
                _popularList.value = response
            }
        }
    }
}