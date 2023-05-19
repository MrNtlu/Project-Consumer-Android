package com.mrntlu.projectconsumer.viewmodels.movie

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.setData
import com.mrntlu.projectconsumer.utils.setPaginationLoading
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val PAGE_KEY = "rv.movie.page"
const val SORT_KEY = "rv.movie.sort"
const val SCROLL_POSITION_KEY = "rv.movie.scroll_position"
const val TAG_KEY = "fetch.tag"

class MovieViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmTag: String,
    @Assisted var isNetworkAvailable: Boolean
): ViewModel() {
    private val _movieList = MutableLiveData<NetworkListResponse<List<Movie>>>()
    val movies: LiveData<NetworkListResponse<List<Movie>>> = _movieList

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
        } else {
            setTag(vmTag)

            startMoviesFetch(sort, true)
        }
    }

    fun startMoviesFetch(newSort: String, refreshAnyway: Boolean = false) {
        if (sort != newSort) {
            setSort(newSort)
            setPagePosition(1)
        } else if (refreshAnyway)
            setPagePosition(1)

        fetchMovies()
    }

    fun fetchMovies() {
        val prevList = arrayListOf<Movie>()
        if (_movieList.value?.data != null && page != 1) {
            prevList.addAll(_movieList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchMovies(page, sort, tag, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _movieList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _movieList.value = setPaginationLoading(prevList)
                    } else
                        _movieList.value = response
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<Movie>()
        viewModelScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                movieRepository.fetchMovies(page, sort, tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                    if (response.isSuccessful()) {
                        tempList.addAll(response.data!!)
                        isPaginationExhausted = response.isPaginationExhausted
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _movieList.value = setData(
                    tempList,
                    isPaginationData = false,
                    isPaginationExhausted = if (isNetworkAvailable) false else isPaginationExhausted
                )
            }
        }
    }

    private fun setTag(newTag: String) {
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

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmTag: String, isNetworkAvailable: Boolean): MovieViewModel
    }

    companion object {
        fun provideMovieViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmTag: String, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmTag, isNetworkAvailable) as T
                }
            }
        }
    }
}