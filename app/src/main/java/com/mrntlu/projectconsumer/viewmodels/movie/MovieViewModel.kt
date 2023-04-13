package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.*
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
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
    private var sort: String = Constants.SortUpcomingRequests[0]

    //TODO Implement process death and orientation change handler
    // https://github.com/MrNtlu/mobillium-case/blob/master/app/src/main/java/com/example/mobilliumcase/viewmodels/MainViewModel.kt
    init {
        fetchUpcomingMovies(sort)
    }

    fun fetchUpcomingMovies(newSort: String) {
        if (sort != newSort) {
            page = 1
            //setSort()
        }

        val prevList = arrayListOf<Movie>()
        if (_upcomingList.value is NetworkListResponse.Success) {
            prevList.addAll((_upcomingList.value as NetworkListResponse.Success<List<Movie>>).data.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchUpcomingMovies(page, sort).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response is NetworkListResponse.Success) {
                        prevList.addAll(response.data)

                        _upcomingList.value = NetworkListResponse.Success(
                            prevList,
                            isPaginationData = response.isPaginationData,
                            isPaginationExhausted = response.isPaginationExhausted,
                        )

                        page = page.plus(1)
                    } else {
                        _upcomingList.value = response
                    }
                }
            }
        }
    }
}