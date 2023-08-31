package com.mrntlu.projectconsumer.viewmodels.main.tv

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.repository.TVRepository
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

private const val TV_PAGE_KEY = "rv.tv.page"
private const val TV_SCROLL_POSITION_KEY = "rv.tv.scroll_position"
private const val TV_TAG_KEY = "tv.fetch.tag"

class TVSeriesViewModel @AssistedInject constructor(
    private val tvRepository: TVRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmTag: String,
    @Assisted var isNetworkAvailable: Boolean,
): ViewModel() {
    private val _tvList = MutableLiveData<NetworkListResponse<List<TVSeries>>>()
    val tvList: LiveData<NetworkListResponse<List<TVSeries>>> = _tvList

    // Process Death variables
    var isRestoringData = false
    private var page: Int = savedStateHandle[TV_PAGE_KEY] ?: 1
    private var tag: String = savedStateHandle[TV_TAG_KEY] ?: FetchType.UPCOMING.tag
    var scrollPosition: Int = savedStateHandle[TV_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1) {
            restoreData()
        } else {
            setTag(vmTag)

            startTVSeriesFetch(true)
        }
    }

    fun startTVSeriesFetch(refreshAnyway: Boolean = false) {
        if (refreshAnyway)
            setPagePosition(1)

        fetchTVSeries()
    }

    fun fetchTVSeries() {
        val prevList = arrayListOf<TVSeries>()
        if (_tvList.value?.data != null && page != 1) {
            prevList.addAll(_tvList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            tvRepository.fetchTVSeries(page, getSort(), tag, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _tvList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _tvList.value = setPaginationLoading(prevList)
                    } else
                        _tvList.value = response
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<TVSeries>()
        viewModelScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                tvRepository.fetchTVSeries(page, getSort(), tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                    if (response.isSuccessful()) {
                        tempList.addAll(response.data!!)
                        isPaginationExhausted = response.isPaginationExhausted
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _tvList.value = setData(
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
            savedStateHandle[TV_TAG_KEY] = tag
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[TV_PAGE_KEY] = page
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[TV_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmTag: String, isNetworkAvailable: Boolean): TVSeriesViewModel
    }

    companion object {
        fun provideTVSeriesViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmTag: String, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmTag, isNetworkAvailable) as T
                }
            }
        }
    }
}