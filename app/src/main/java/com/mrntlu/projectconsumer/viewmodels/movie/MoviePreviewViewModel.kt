package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.main.movie.retrofit.MoviePaginationResponse
import com.mrntlu.projectconsumer.repository.MoviePreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MoviePreviewViewModel @Inject constructor(
    private val moviePreviewRepository: MoviePreviewRepository,
): ViewModel() {
    private val _upcomingList = MutableLiveData<NetworkResponse<MoviePaginationResponse>>()
    val upcomingMovies: LiveData<NetworkResponse<MoviePaginationResponse>> = _upcomingList

    private val _popularList = MutableLiveData<NetworkResponse<MoviePaginationResponse>>()
    val popularMovies: LiveData<NetworkResponse<MoviePaginationResponse>> = _popularList

    init {
        fetchUpcomingMovies()
        fetchPopularMovies()
    }

    fun fetchUpcomingMovies() = networkResponseFlowCollector(
        moviePreviewRepository.fetchUpcomingMovies()
    ) { response ->
        _upcomingList.value = response
    }

    fun fetchPopularMovies() = networkResponseFlowCollector(
        moviePreviewRepository.fetchPopularMovies()
    ) { response ->
        _popularList.value = response
    }
}