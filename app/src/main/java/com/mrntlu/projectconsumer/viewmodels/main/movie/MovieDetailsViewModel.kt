package com.mrntlu.projectconsumer.viewmodels.main.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
): ViewModel() {
    private val _movieDetails = MutableLiveData<NetworkResponse<DataResponse<MovieDetails>>>()
    val movieDetails: LiveData<NetworkResponse<DataResponse<MovieDetails>>> = _movieDetails

    fun getMovieDetails(id: String) = networkResponseFlowCollector(
        movieRepository.getMovieDetails(id)
    ) { response ->
        _movieDetails.value = response
    }
}