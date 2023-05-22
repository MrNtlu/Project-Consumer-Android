package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MoviePreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoviePreviewViewModel @Inject constructor(
    private val moviePreviewRepository: MoviePreviewRepository,
): ViewModel() {
    private val _upcomingList = MutableLiveData<NetworkResponse<DataPaginationResponse<Movie>>>()
    val upcomingMovies: LiveData<NetworkResponse<DataPaginationResponse<Movie>>> = _upcomingList

    private val _popularList = MutableLiveData<NetworkResponse<DataPaginationResponse<Movie>>>()
    val popularMovies: LiveData<NetworkResponse<DataPaginationResponse<Movie>>> = _popularList

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