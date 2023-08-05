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
import com.mrntlu.projectconsumer.repository.AnimeRepository
import com.mrntlu.projectconsumer.repository.GameRepository
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.repository.TVRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.SortRequests
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

private const val DISCOVER_PAGE_KEY = "rv.discover.page"
private const val DISCOVER_SCROLL_POSITION_KEY = "rv.discover.scroll_position"
private const val DISCOVER_CONTENT_TYPE_KEY = "rv.discover.content_type"
private const val DISCOVER_SORT_KEY = "rv.discover.sort"
private const val DISCOVER_STATUS_KEY = "rv.discover.status"
private const val DISCOVER_GENRE_KEY = "rv.discover.genre"
private const val DISCOVER_FROM_KEY = "rv.discover.from"
private const val DISCOVER_TO_KEY = "rv.discover.to"
private const val DISCOVER_ANIME_THEME_KEY = "rv.discover.anime.theme"
private const val DISCOVER_ANIME_DEMOGRAPHICS_KEY = "rv.discover.anime.demographics"
private const val DISCOVER_GAME_TBA_KEY = "rv.discover.game.tba"
private const val DISCOVER_GAME_PLATFORM_KEY = "rv.discover.game.platform"

class DiscoverListViewModel @AssistedInject constructor(
    private val movieRepository: MovieRepository,
    private val tvRepository: TVRepository,
    private val animeRepository: AnimeRepository,
    private val gameRepository: GameRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @Assisted private val vmGenre: String?,
    @Assisted private val vmContentType: Constants.ContentType,
    @Assisted var isNetworkAvailable: Boolean
): ViewModel() {
    private val _discoverList = MutableLiveData<NetworkListResponse<List<ContentModel>>>()
    val discoverResults: LiveData<NetworkListResponse<List<ContentModel>>> = _discoverList

    // Process Death variables
    var isRestoringData = false
    private var contentType: Constants.ContentType = savedStateHandle[DISCOVER_CONTENT_TYPE_KEY] ?: vmContentType
    private var page: Int = savedStateHandle[DISCOVER_PAGE_KEY] ?: 1
    var sort: String = savedStateHandle[DISCOVER_SORT_KEY] ?: SortRequests[0].request
        private set
    var status: String? = savedStateHandle[DISCOVER_STATUS_KEY]
        private set
    var genre: String? = savedStateHandle[DISCOVER_GENRE_KEY] ?: vmGenre
        private set
    var from: Int? = savedStateHandle[DISCOVER_FROM_KEY]
        private set
    var to: Int? = savedStateHandle[DISCOVER_TO_KEY]
        private set
    var animeTheme: String? = savedStateHandle[DISCOVER_ANIME_THEME_KEY]
        private set
    var animeDemographics: String? = savedStateHandle[DISCOVER_ANIME_DEMOGRAPHICS_KEY]
        private set
    var gameTBA: Boolean? = savedStateHandle[DISCOVER_GAME_TBA_KEY]
        private set
    var gamePlatform: String? = savedStateHandle[DISCOVER_GAME_PLATFORM_KEY]
        private set
    var scrollPosition: Int = savedStateHandle[DISCOVER_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        if (page != 1)
            restoreData()
        else {
            setContentType(vmContentType)
            setGenre(vmGenre)

            startDiscoveryFetch(contentType, sort, null, genre, null, null)
        }
    }

    fun startDiscoveryFetch(
        newContentType: Constants.ContentType,
        newSort: String,
        newStatus: String?,
        newGenre: String?,
        newFrom: Int? = null,
        newTo: Int? = null,
        newAnimeTheme: String? = null,
        newAnimeDemographics: String? = null,
        newGameTBA: Boolean? = null,
        newGamePlatform: String? = null,
        refreshAnyway: Boolean = false,
    ) {
        if (
            newContentType != contentType || newSort != sort || newStatus != status || newGenre != genre ||
            ((newContentType == Constants.ContentType.MOVIE || newContentType == Constants.ContentType.TV) && (newFrom != from || newTo != to)) ||
            (newContentType == Constants.ContentType.ANIME && (newAnimeTheme != animeTheme || newAnimeDemographics != animeDemographics)) ||
            (newContentType == Constants.ContentType.GAME && (newGameTBA != gameTBA || newGamePlatform != gamePlatform)) || refreshAnyway
        ) {
            setContentType(newContentType)
            setSort(newSort)
            setStatus(newStatus)
            setGenre(newGenre)
            setFrom(newFrom)
            setTo(newTo)
            setAnimeTheme(newAnimeTheme)
            setAnimeDemographics(newAnimeDemographics)
            setGameTBA(newGameTBA)
            setGamePlatform(newGamePlatform)
            setPagePosition(1)
        }

        discoverContent()
    }

    fun discoverContent() {
        val prevList = arrayListOf<ContentModel>()
        if (_discoverList.value?.data != null && page != 1) {
            prevList.addAll(_discoverList.value!!.data!!.toCollection(ArrayList()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            val flowCollector = when(contentType) {
                Constants.ContentType.ANIME -> animeRepository.getAnimeBySortFilter(
                    page, sort, status, genre, animeDemographics, animeTheme, null, isNetworkAvailable,
                )
                Constants.ContentType.MOVIE -> movieRepository.getMovieBySortFilter(
                    page, sort, status, null, genre, from, to, isNetworkAvailable
                )
                Constants.ContentType.TV -> tvRepository.getTVSeriesBySortFilter(
                    page, sort, status, null, null, genre, from, to, isNetworkAvailable
                )
                Constants.ContentType.GAME -> gameRepository.getGameBySortFilter(
                    page, sort, gameTBA, genre, gamePlatform, isNetworkAvailable,
                )
            }

            flowCollector.collect { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful()) {
                        prevList.addAll(response.data!!)

                        _discoverList.value = setData(prevList, response.isPaginationData, response.isPaginationExhausted)

                        if (!response.isPaginationExhausted)
                            setPagePosition(page.plus(1))
                    } else if (response.isPaginating) {
                        _discoverList.value = setPaginationLoading(prevList)
                    } else {
                        val contentResponse: NetworkListResponse<List<ContentModel>> = NetworkListResponse(
                            response.data, response.isLoading, response.isPaginating, response.isFailed,
                            response.isPaginationData, response.isPaginationExhausted, response.errorMessage
                        )
                        _discoverList.value = contentResponse
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
                    Constants.ContentType.ANIME -> animeRepository.getAnimeBySortFilter(
                        page, sort, status, genre, animeDemographics, animeTheme, null,
                        isNetworkAvailable, isRestoringData = true
                    )
                    Constants.ContentType.MOVIE -> movieRepository.getMovieBySortFilter(
                        page, sort, status, null, genre, from, to,
                        isNetworkAvailable, isRestoringData = true,
                    )
                    Constants.ContentType.TV -> tvRepository.getTVSeriesBySortFilter(
                        page, sort, status, null, null, genre, from, to,
                        isNetworkAvailable, isRestoringData = true,
                    )
                    Constants.ContentType.GAME -> gameRepository.getGameBySortFilter(
                        page, sort, gameTBA, genre, gamePlatform,
                        isNetworkAvailable, isRestoringData = true,
                    )
                }

                flowCollector.collect { response ->
                    if (response.isSuccessful()) {
                        tempList.addAll(response.data!!)
                        isPaginationExhausted = response.isPaginationExhausted
                    }
                }

                withContext(Dispatchers.Main) {
                    _discoverList.value = setData(
                        tempList,
                        isPaginationData = false,
                        isPaginationExhausted = if (isNetworkAvailable) false else isPaginationExhausted
                    )
                }
            }
        }
    }

    private fun setContentType(newContentType: Constants.ContentType) {
        contentType = newContentType
        savedStateHandle[DISCOVER_CONTENT_TYPE_KEY] = contentType
    }

    private fun setPagePosition(newPage: Int) {
        page = newPage
        savedStateHandle[DISCOVER_PAGE_KEY] = page
    }

    private fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[DISCOVER_SORT_KEY] = newSort
    }

    private fun setStatus(newStatus: String?) {
        status = newStatus
        savedStateHandle[DISCOVER_STATUS_KEY] = newStatus
    }

    private fun setGenre(newGenre: String?) {
        genre = newGenre
        savedStateHandle[DISCOVER_GENRE_KEY] = newGenre
    }

    private fun setFrom(newFrom: Int?) {
        from = newFrom
        savedStateHandle[DISCOVER_FROM_KEY] = newFrom
    }

    private fun setTo(newTo: Int?) {
        to = newTo
        savedStateHandle[DISCOVER_TO_KEY] = newTo
    }

    private fun setAnimeTheme(newAnimeTheme: String?) {
        animeTheme = newAnimeTheme
        savedStateHandle[DISCOVER_ANIME_THEME_KEY] = newAnimeTheme
    }

    private fun setAnimeDemographics(newAnimeDemographics: String?) {
        animeDemographics = newAnimeDemographics
        savedStateHandle[DISCOVER_ANIME_DEMOGRAPHICS_KEY] = newAnimeDemographics
    }

    private fun setGameTBA(newGameTBA: Boolean?) {
        gameTBA = newGameTBA
        savedStateHandle[DISCOVER_GAME_TBA_KEY] = newGameTBA
    }

    private fun setGamePlatform(newAnimeTheme: String?) {
        gamePlatform = newAnimeTheme
        savedStateHandle[DISCOVER_GAME_PLATFORM_KEY] = newAnimeTheme
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[DISCOVER_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle, vmGenre: String?, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): DiscoverListViewModel
    }

    companion object {
        fun provideDiscoverListViewModelFactory(factory: Factory, owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null, vmGenre: String?, vmContentType: Constants.ContentType, isNetworkAvailable: Boolean): AbstractSavedStateViewModelFactory {
            return object: AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                    return factory.create(handle, vmGenre, vmContentType, isNetworkAvailable) as T
                }
            }
        }
    }
}