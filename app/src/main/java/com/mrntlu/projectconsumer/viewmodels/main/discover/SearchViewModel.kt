package com.mrntlu.projectconsumer.viewmodels.main.discover

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.repository.TVRepository
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

const val SEARCH_PAGE_KEY = "rv.search.page"
const val SEARCH_SCROLL_POSITION_KEY = "rv.search.scroll_position"
const val SEARCH_TEXT_KEY = "rv.search.text"
const val SEARCH_CONTENT_TYPE_KEY = "rv.search.content_type"

class SearchViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    private val tvRepository: TVRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmSearch: String,
    @Assisted private val vmContentType: Constants.ContentType,
    @Assisted var isNetworkAvailable: Boolean
): ViewModel() {
    private val _searchList = MutableLiveData<NetworkListResponse<List<ContentModel>>>()
    val searchResults: LiveData<NetworkListResponse<List<ContentModel>>> = _searchList

    // Process Death variables
    var isRestoringData = false
    private var contentType: Constants.ContentType = savedStateHandle[SEARCH_CONTENT_TYPE_KEY] ?: vmContentType
    private var page: Int = savedStateHandle[SEARCH_PAGE_KEY] ?: 1
    private var search: String = savedStateHandle[SEARCH_TEXT_KEY] ?: vmSearch
    var scrollPosition: Int = savedStateHandle[SEARCH_SCROLL_POSITION_KEY] ?: 0
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

        searchContentByTitle()
    }

    fun searchContentByTitle() {
        val prevList = arrayListOf<ContentModel>()
        if (_searchList.value?.data != null && page != 1) {
            prevList.addAll(_searchList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            val flowCollector = when(contentType) {
                Constants.ContentType.ANIME -> TODO()
                Constants.ContentType.MOVIE -> movieRepository.searchMoviesByTitle(search, page, isNetworkAvailable)
                Constants.ContentType.TV -> tvRepository.searchTVSeriesByTitle(search, page, isNetworkAvailable)
                Constants.ContentType.GAME -> TODO()
            }

            flowCollector.collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _searchList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _searchList.value = setPaginationLoading(prevList)
                    } else {
                        val contentResponse: NetworkListResponse<List<ContentModel>> = NetworkListResponse(
                            response.data, response.isLoading, response.isPaginating, response.isFailed,
                            response.isPaginationData, response.isPaginationExhausted, response.errorMessage
                        )
                        _searchList.value = contentResponse
                    }
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<ContentModel>()

        viewModelScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                val flowCollector = when(contentType) {
                    Constants.ContentType.ANIME -> TODO()
                    Constants.ContentType.MOVIE -> movieRepository.searchMoviesByTitle(search, page, isNetworkAvailable, isRestoringData = true)
                    Constants.ContentType.TV -> tvRepository.searchTVSeriesByTitle(search, page, isNetworkAvailable, isRestoringData = true)
                    Constants.ContentType.GAME -> TODO()
                }

                flowCollector.collect { response ->
                    if (response.isSuccessful()) {
                        tempList.addAll(response.data!!)
                        isPaginationExhausted = response.isPaginationExhausted
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _searchList.value = setData(
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
        savedStateHandle[SEARCH_PAGE_KEY] = page
    }

    private fun setSearch(newSearch: String) {
        search = newSearch
        savedStateHandle[SEARCH_TEXT_KEY] = newSearch
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[SEARCH_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmSearch: String, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): SearchViewModel
    }

    companion object {
        fun provideSearchViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmSearch: String, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmSearch, vmContentType, isNetworkAvailable) as T
                }
            }
        }
    }
}