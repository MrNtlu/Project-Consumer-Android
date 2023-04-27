package com.mrntlu.projectconsumer.viewmodels.movie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val PAGE_KEY = "rv.movie.page"
const val SORT_KEY = "rv.movie.sort"
const val SCROLL_POSITION_KEY = "rv.movie.scroll_position"
const val TAG_KEY = "fetch.tag"

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
): ViewModel() {
    private val _movieList = MutableLiveData<NetworkListResponse<List<Movie>>>()
    val movies: LiveData<NetworkListResponse<List<Movie>>> = _movieList

    var isNetworkAvailable = true

    // Process Death variables
    var isRestoringData = false
    private var page: Int = savedStateHandle[PAGE_KEY] ?: 1
    private var tag: String = savedStateHandle[TAG_KEY] ?: FetchType.UPCOMING.tag
    private var sort: String = savedStateHandle[SORT_KEY] ?: Constants.SortUpcomingRequests[0].request
    var scrollPosition: Int = savedStateHandle[SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1) {
            restoreData()
        }
    }

    fun refreshData() {
        setPagePosition(1)

        when(tag) {
            FetchType.UPCOMING.tag -> fetchUpcomingMovies(sort)
            FetchType.POPULAR.tag -> fetchPopularMovies(sort)
        }
    }

    fun fetchUpcomingMovies(newSort: String) {
        setTag(FetchType.UPCOMING.tag)

        if (sort != newSort) {
            setPagePosition(1)
            setSort(newSort)
        }

        fetchMovies()
    }

    fun fetchPopularMovies(newSort: String) {
        setTag(FetchType.POPULAR.tag)

        if (sort != newSort) {
            setPagePosition(1)
            setSort(newSort)
        }

        fetchMovies()
    }

    fun fetchMovies() {
        val prevList = arrayListOf<Movie>()
        if (_movieList.value is NetworkListResponse.Success && page != 1) {
            prevList.addAll((_movieList.value as NetworkListResponse.Success<List<Movie>>).data.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchMovies(page, sort, tag, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response is NetworkListResponse.Success) {
                        prevList.addAll(response.data)

                        _movieList.value = NetworkListResponse.Success(
                            prevList,
                            isPaginationData = response.isPaginationData,
                            isPaginationExhausted = response.isPaginationExhausted,
                        )

                        if (!response.isPaginationExhausted) {
                            setPagePosition(page.plus(1))
                        }
                    } else {
                        _movieList.value = response
                    }
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<Movie>()
        viewModelScope.launch(Dispatchers.IO) {
            for (p in 1..page) {
                val job = launch(Dispatchers.IO) {
                    movieRepository.fetchMovies(p, sort, tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                        if (response is NetworkListResponse.Success) {
                            tempList.addAll(response.data)
                            isPaginationExhausted = response.isPaginationExhausted
                        }
                    }
                }
                job.join()
            }
            withContext(Dispatchers.Main) {
                _movieList.value = NetworkListResponse.Success(
                    tempList,
                    isPaginationData = false,
                    isPaginationExhausted = isPaginationExhausted,
                )
            }
        }
    }

    fun setTag(newTag: String) {
        if (tag != newTag) {
            tag = newTag
            savedStateHandle[TAG_KEY] = tag
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[PAGE_KEY] = page
    }

    private fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[SORT_KEY] = sort
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[SCROLL_POSITION_KEY] = scrollPosition
        }
    }
}