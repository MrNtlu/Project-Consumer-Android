package com.mrntlu.projectconsumer.viewmodels.main.anime

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.repository.AnimeRepository
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

private const val ANIME_PAGE_KEY = "rv.anime.page"
private const val ANIME_SORT_KEY = "rv.anime.sort"
private const val ANIME_SCROLL_POSITION_KEY = "rv.anime.scroll_position"
private const val ANIME_TAG_KEY = "anime.fetch.tag"

class AnimeViewModel @AssistedInject constructor(
    private val animeRepository: AnimeRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmTag: String,
    @Assisted var isNetworkAvailable: Boolean,
): ViewModel() {
    private val _animeList = MutableLiveData<NetworkListResponse<List<Anime>>>()
    val animes: LiveData<NetworkListResponse<List<Anime>>> = _animeList

    // Process Death variables
    var isRestoringData = false
    private var page: Int = savedStateHandle[ANIME_PAGE_KEY] ?: 1
    private var tag: String = savedStateHandle[ANIME_TAG_KEY] ?: FetchType.UPCOMING.tag
    private var sort: String = savedStateHandle[ANIME_SORT_KEY] ?: Constants.SortUpcomingRequests[0].request
    var scrollPosition: Int = savedStateHandle[ANIME_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1) {
            restoreData()
        } else {
            setTag(vmTag)

            startAnimeFetch(sort, true)
        }
    }

    fun startAnimeFetch(newSort: String, refreshAnyway: Boolean = false) {
        if (sort != newSort) {
            setSort(newSort)
            setPagePosition(1)
        } else if (refreshAnyway)
            setPagePosition(1)

        fetchAnime()
    }

    fun fetchAnime() {
        val prevList = arrayListOf<Anime>()
        if (_animeList.value?.data != null && page != 1) {
            prevList.addAll(_animeList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            animeRepository.fetchAnimes(page, sort, tag, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _animeList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _animeList.value = setPaginationLoading(prevList)
                    } else
                        _animeList.value = response
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<Anime>()
        viewModelScope.launch(Dispatchers.IO) {
            animeRepository.fetchAnimes(page, sort, tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                if (response.isSuccessful()) {
                    tempList.addAll(response.data!!)
                    isPaginationExhausted = response.isPaginationExhausted
                }
            }

            withContext(Dispatchers.Main) {
                _animeList.value = setData(
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
            savedStateHandle[ANIME_TAG_KEY] = tag
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[ANIME_PAGE_KEY] = page
    }

    private fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[ANIME_SORT_KEY] = sort
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[ANIME_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmTag: String, isNetworkAvailable: Boolean): AnimeViewModel
    }

    companion object {
        fun provideAnimeViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmTag: String, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmTag, isNetworkAvailable) as T
                }
            }
        }
    }
}