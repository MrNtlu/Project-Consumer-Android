package com.mrntlu.projectconsumer.viewmodels.main.game

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.repository.GameRepository
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

private const val GAME_PAGE_KEY = "rv.game.page"
private const val GAME_SORT_KEY = "rv.game.sort"
private const val GAME_SCROLL_POSITION_KEY = "rv.game.scroll_position"
private const val GAME_TAG_KEY = "game.fetch.tag"

class GameViewModel @AssistedInject constructor(
    private val gameRepository: GameRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmTag: String,
    @Assisted var isNetworkAvailable: Boolean,
): ViewModel() {
    private val _gameList = MutableLiveData<NetworkListResponse<List<Game>>>()
    val games: LiveData<NetworkListResponse<List<Game>>> = _gameList

    // Process Death variables
    var isRestoringData = false
    private var page: Int = savedStateHandle[GAME_PAGE_KEY] ?: 1
    private var tag: String = savedStateHandle[GAME_TAG_KEY] ?: FetchType.UPCOMING.tag
    private var sort: String = savedStateHandle[GAME_SORT_KEY] ?: Constants.SortUpcomingRequests[0].request
    var scrollPosition: Int = savedStateHandle[GAME_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1) {
            restoreData()
        } else {
            setTag(vmTag)

            startGamesFetch(sort, true)
        }
    }

    fun startGamesFetch(newSort: String, refreshAnyway: Boolean = false) {
        if (sort != newSort) {
            setSort(newSort)
            setPagePosition(1)
        } else if (refreshAnyway)
            setPagePosition(1)

        fetchGames()
    }

    fun fetchGames() {
        val prevList = arrayListOf<Game>()
        if (_gameList.value?.data != null && page != 1) {
            prevList.addAll(_gameList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.fetchGames(page, sort, tag, isNetworkAvailable).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _gameList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _gameList.value = setPaginationLoading(prevList)
                    } else
                        _gameList.value = response
                }
            }
        }
    }

    private fun restoreData() {
        isRestoringData = true

        var isPaginationExhausted = false
        val tempList = arrayListOf<Game>()
        viewModelScope.launch(Dispatchers.IO) {
            gameRepository.fetchGames(page, sort, tag, isNetworkAvailable, isRestoringData = true).collect { response ->
                if (response.isSuccessful()) {
                    tempList.addAll(response.data!!)
                    isPaginationExhausted = response.isPaginationExhausted
                }
            }

            withContext(Dispatchers.Main) {
                _gameList.value = setData(
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
            savedStateHandle[GAME_TAG_KEY] = tag
        }
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[GAME_PAGE_KEY] = page
    }

    private fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[GAME_SORT_KEY] = sort
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[GAME_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmTag: String, isNetworkAvailable: Boolean): GameViewModel
    }

    companion object {
        fun provideGameViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmTag: String, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmTag, isNetworkAvailable) as T
                }
            }
        }
    }
}