package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.*
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
): ViewModel() {
    private val _upcomingList = MutableLiveData<NetworkListResponse<List<Movie>>>()
    val upcomingMovies: LiveData<NetworkListResponse<List<Movie>>> = _upcomingList

    private var page: Int = 1

    init {
        fetchUpcomingMovies(Constants.SortUpcomingRequests[0])
    }

    fun fetchUpcomingMovies(sort: String) = viewModelScope.launch(Dispatchers.IO) {

        movieRepository.fetchUpcomingMovies(page, sort).collect { response ->
            withContext(Dispatchers.Main) {
                _upcomingList.value = response
            }
        }
    }
}