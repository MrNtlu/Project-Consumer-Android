package com.mrntlu.projectconsumer.viewmodels.main.movie

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
import com.mrntlu.projectconsumer.utils.Constants.SortRequests
import com.mrntlu.projectconsumer.utils.Constants.SortUpcomingRequests
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

private const val MOVIE_PAGE_KEY = "rv.movie.page"
private const val MOVIE_SCROLL_POSITION_KEY = "rv.movie.scroll_position"
private const val MOVIE_TAG_KEY = "movie.fetch.tag"

class MovieViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmTag: String,
    @Assisted var isNetworkAvailable: Boolean,
): ViewModel() {
    private val _movieList = MutableLiveData<NetworkListResponse<List<Movie>>>()
    val movies: LiveData<NetworkListResponse<List<Movie>>> = _movieList

    // Process Death variables
    var isRestoringData = false
    private var page: Int = savedStateHandle[MOVIE_PAGE_KEY] ?: 1
    private var tag: String = savedStateHandle[MOVIE_TAG_KEY] ?: FetchType.UPCOMING.tag
    var scrollPosition: Int = savedStateHandle[MOVIE_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1) {
            restoreData()
        } else {
            setTag(vmTag)

            startMoviesFetch(true)
        }
    }

    fun startMoviesFetch(refreshAnyway: Boolean = false) {
        if (refreshAnyway)
            setPagePosition(1)

        fetchMovies()
    }

    fun fetchMovies() {
        val prevList = arrayListOf<Movie>()
        if (_movieList.value?.data != null && page != 1) {
            prevList.addAll(_movieList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.fetchMovies(page, getSort(), tag, isNetworkAvailable).collect { response ->
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
            movieRepository.fetchMovies(page, getSort(), tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                if (response.isSuccessful()) {
                    tempList.addAll(response.data!!)
                    isPaginationExhausted = response.isPaginationExhausted
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

    private fun getSort(): String = when(tag) {
        FetchType.UPCOMING.tag -> SortUpcomingRequests.request
        FetchType.TOP.tag -> SortRequests[1].request
        else -> SortRequests[0].request
    }

    private fun setTag(newTag: String) {
        if (tag != newTag) {
            tag = newTag
            savedStateHandle[MOVIE_TAG_KEY] = tag
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[MOVIE_PAGE_KEY] = page
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[MOVIE_SCROLL_POSITION_KEY] = scrollPosition
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