package com.mrntlu.projectconsumer.viewmodels.main.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
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
    private val _previewList = MutableLiveData<NetworkResponse<PreviewResponse<Movie>>>()
    val previewList: LiveData<NetworkResponse<PreviewResponse<Movie>>> = _previewList

    init {
        fetchPreviewMovies()
    }

    fun fetchPreviewMovies() = networkResponseFlowCollector(
        moviePreviewRepository.fetchPreviewMovies()
    ) { response ->
        _previewList.value = response
    }
}