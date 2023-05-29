package com.mrntlu.projectconsumer.viewmodels.movie

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.utils.Constants
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

const val SEARCH_MOVIE_PAGE_KEY = "rv.movie.search.page"
const val SEARCH_MOVIE_SCROLL_POSITION_KEY = "rv.movie.search.scroll_position"
const val SEARCH_MOVIE_TEXT_KEY = "rv.movie.search.text"
const val SEARCH_CONTENT_TYPE_KEY = "rv.search.content_type"

class MovieSearchViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmSearch: String,
    @Assisted private val vmContentType: Constants.ContentType,
    @Assisted var isNetworkAvailable: Boolean
): ViewModel() {
    private val _movieList = MutableLiveData<NetworkListResponse<List<ContentModel>>>()
    val movies: LiveData<NetworkListResponse<List<ContentModel>>> = _movieList

    // Process Death variables
    var isRestoringData = false
    private var contentType: Constants.ContentType = savedStateHandle[SEARCH_CONTENT_TYPE_KEY] ?: vmContentType
    private var page: Int = savedStateHandle[SEARCH_MOVIE_PAGE_KEY] ?: 1
    private var search: String = savedStateHandle[SEARCH_MOVIE_TEXT_KEY] ?: vmSearch
    var scrollPosition: Int = savedStateHandle[SEARCH_MOVIE_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1 || search != vmSearch) {
            restoreData()
        } else {
            setContentType(vmContentType)
            setSearch(vmSearch)

            startMoviesFetch(search, true)
        }
    }

    fun startMoviesFetch(newSearch: String, refreshAnyway: Boolean = false) {
        if (newSearch != search) {
            setSearch(newSearch)
            setPagePosition(1)
        } else if (refreshAnyway)
            setPagePosition(1)

        searchMoviesByTitle()
    }

    fun searchMoviesByTitle() {
        val prevList = arrayListOf<ContentModel>()
        if (_movieList.value?.data != null && page != 1) {
            prevList.addAll(_movieList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            movieRepository.searchMoviesByTitle(search, page, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _movieList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _movieList.value = setPaginationLoading(prevList)
                    } else {
                        val contentResponse: NetworkListResponse<List<ContentModel>> = NetworkListResponse(
                            response.data, response.isLoading, response.isPaginating, response.isFailed,
                            response.isPaginationData, response.isPaginationExhausted, response.errorMessage
                        )
                        _movieList.value = contentResponse
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
            launch(Dispatchers.IO) {
                movieRepository.searchMoviesByTitle(search, page, isNetworkAvailable, isRestoringData = true).collect { response ->
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

    private fun setContentType(newContentType: Constants.ContentType) {
        contentType = newContentType
        savedStateHandle[SEARCH_CONTENT_TYPE_KEY] = contentType
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[SEARCH_MOVIE_PAGE_KEY] = page
    }

    private fun setSearch(newSearch: String) {
        search = newSearch
        savedStateHandle[SEARCH_MOVIE_TEXT_KEY] = newSearch
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[SEARCH_MOVIE_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmSearch: String, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): MovieSearchViewModel
    }

    companion object {
        fun provideMovieViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmSearch: String, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmSearch, vmContentType, isNetworkAvailable) as T
                }
            }
        }
    }
}