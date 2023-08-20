package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.AnimeList
import com.mrntlu.projectconsumer.models.main.userList.GameList
import com.mrntlu.projectconsumer.models.main.userList.MovieList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesList
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.models.main.userList.convertToAnimeList
import com.mrntlu.projectconsumer.models.main.userList.convertToGameList
import com.mrntlu.projectconsumer.models.main.userList.convertToMovieList
import com.mrntlu.projectconsumer.models.main.userList.convertToTVSeriesList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val USER_LIST_SORT_KEY = "rv.ul.sort"
const val USER_LIST_SCROLL_POSITION_KEY = "rv.ul.scroll_position"
const val USER_LIST_SEARCH_KEY = "rv.ul.search"
const val USER_LIST_CONTENT_TYPE_KEY = "rv.ul.content_type"

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserListRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    // Scroll Y
    val totalYScroll: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setTotalYPosition(dy: Int) {
        val newTotal = this.totalYScroll.value?.plus(dy)
        this.totalYScroll.value = if (newTotal != null) {
            if (newTotal > 200) 201
            else if (newTotal < 0) 0
            else newTotal
        } else null
    }

    fun resetTotalYPosition() {
        this.totalYScroll.value = 0
    }

    // Variable for detecting orientation change
    var didOrientationChange = false
    private var currentOrientation: Orientation = Orientation.Idle

    fun setNewOrientation(newOrientation: Orientation) {
        if (currentOrientation == Orientation.Idle) {
            currentOrientation = newOrientation
        } else if (newOrientation != currentOrientation) {
            resetTotalYPosition()
            didOrientationChange = true

            currentOrientation = newOrientation
        }
    }

    private val _userList = MutableLiveData<NetworkResponse<DataResponse<UserList>>>()
    val userList: LiveData<NetworkResponse<DataResponse<UserList>>> = _userList

    private var searchHolder: UserList? = null
    var contentType: Constants.ContentType = savedStateHandle[USER_LIST_CONTENT_TYPE_KEY] ?: Constants.ContentType.MOVIE
        private set
    var search: String? = savedStateHandle[USER_LIST_SEARCH_KEY]
        private set

    // Process Death variables
    var sort: String = savedStateHandle[USER_LIST_SORT_KEY] ?: Constants.SortUserListRequests[0].request
        private set
    var scrollPosition: Int = savedStateHandle[USER_LIST_SCROLL_POSITION_KEY] ?: 0
        private set

    init {
        getUserList()
    }

    fun getUserList() = networkResponseFlowCollector(
        repository.getUserList(sort)
    ) { response ->
        _userList.value = response
    }

    fun handleSearchHolderOperation(id: String, data: UserListModel?, response: NetworkResponse<*>, operation: OperationEnum) {
        val currentSearchList = when(contentType) {
            Constants.ContentType.ANIME -> searchHolder?.animeList
            Constants.ContentType.MOVIE -> searchHolder?.movieList
            Constants.ContentType.TV -> searchHolder?.tvList
            Constants.ContentType.GAME -> searchHolder?.gameList
        }?.toMutableList()

        if (response is NetworkResponse.Success && currentSearchList?.isNotEmpty() == true) {
            val index = currentSearchList.indexOfFirst {
                it.id == id
            }

            if (index >= 0) {
                when(operation) {
                    OperationEnum.Update -> {
                        data?.let { ulm ->
                            when(contentType) {
                                Constants.ContentType.ANIME -> {
                                    val item = searchHolder!!.animeList[index]

                                    currentSearchList[index] = AnimeList(
                                        id, ulm.contentStatus, ulm.score, ulm.timesFinished, ulm.mainAttribute!!, ulm.contentId, ulm.contentExternalId,
                                        item.title, item.titleOriginal, item.imageUrl, item.totalSeasons,
                                    )
                                    searchHolder?.animeList = currentSearchList.toList().map { it.convertToAnimeList() }
                                }
                                Constants.ContentType.MOVIE -> {
                                    val item = searchHolder!!.movieList[index]

                                    currentSearchList[index] = MovieList(
                                        id, ulm.contentStatus, ulm.score, ulm.timesFinished, ulm.contentId, ulm.contentExternalId,
                                        item.title, item.titleOriginal, item.imageUrl,
                                    )
                                    searchHolder?.movieList = currentSearchList.toList().map { it.convertToMovieList() }
                                }
                                Constants.ContentType.TV -> {
                                    val item = searchHolder!!.tvList[index]

                                    currentSearchList[index] = TVSeriesList(
                                        id, ulm.contentStatus, ulm.score, ulm.timesFinished, ulm.mainAttribute, ulm.subAttribute,
                                        ulm.contentId, ulm.contentExternalId, item.title, item.titleOriginal,
                                        item.imageUrl, item.totalEpisodes, item.totalSeasons,
                                    )

                                    searchHolder?.tvList = currentSearchList.toList().map { it.convertToTVSeriesList() }
                                }
                                Constants.ContentType.GAME -> {
                                    val item = searchHolder!!.gameList[index]

                                    currentSearchList[index] = GameList(
                                        id, ulm.contentStatus, ulm.score, ulm.timesFinished, ulm.mainAttribute, ulm.contentId,
                                        ulm.contentExternalId, item.title, item.titleOriginal, item.imageUrl,
                                    )

                                    searchHolder?.gameList = currentSearchList.toList().map { it.convertToGameList() }
                                }
                            }
                        }
                    }
                    OperationEnum.Delete -> {
                        currentSearchList.removeAt(index)

                        when(contentType) {
                            Constants.ContentType.ANIME -> searchHolder?.animeList = currentSearchList.toList().map { it.convertToAnimeList() }
                            Constants.ContentType.MOVIE -> searchHolder?.movieList = currentSearchList.toList().map { it.convertToMovieList() }
                            Constants.ContentType.TV -> searchHolder?.tvList = currentSearchList.toList().map { it.convertToTVSeriesList() }
                            Constants.ContentType.GAME -> searchHolder?.gameList = currentSearchList.toList().map { it.convertToGameList() }
                        }
                    }
                }
            }
        }
    }

    fun deleteUserList(body: DeleteUserListBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserList(body).collect { response ->
                withContext(Dispatchers.Main) {
                    handleSearchHolderOperation(body.id, null, response, OperationEnum.Delete)

                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[USER_LIST_SORT_KEY] = sort
    }

    fun setScrollPosition(newPosition: Int) {
        if (!didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[USER_LIST_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    fun search(search: String?) {
        setSearch(search)

        if (search != null && search.isNotEmptyOrBlank() && userList.value is NetworkResponse.Success) {
            val userListResponse = userList.value as NetworkResponse.Success<DataResponse<UserList>>

            val currentUserList = if (
                searchHolder != null &&
                getContentList(searchHolder!!).size >
                getContentList(userListResponse.data.data).size
            ) {
                searchHolder!!.copy()
            } else {
                searchHolder = userListResponse.data.data.copy()

                userListResponse.data.data.copy()
            }

            val searchList = getContentList(currentUserList).filter {
                it.titleOriginal.startsWith(search, ignoreCase = true) || it.titleOriginal.contains(search, ignoreCase = true) ||
                        it.title.startsWith(search, ignoreCase = true) || it.title.contains(search, ignoreCase = true)
            }.toMutableList().toCollection(ArrayList())

            when(contentType) {
                Constants.ContentType.ANIME -> currentUserList.animeList = searchList.toList().map { it.convertToAnimeList() }
                Constants.ContentType.MOVIE -> currentUserList.movieList = searchList.toList().map { it.convertToMovieList() }
                Constants.ContentType.TV -> currentUserList.tvList = searchList.toList().map { it.convertToTVSeriesList() }
                Constants.ContentType.GAME -> currentUserList.gameList = searchList.toList().map { it.convertToGameList() }
            }

            _userList.value = NetworkResponse.Success(DataResponse(currentUserList))
        } else if (searchHolder != null) {
            resetTotalYPosition()
            resetSearch()
        }
    }

    private fun resetSearch() {
        if (searchHolder != null) {
            setSearch(null)
            _userList.value = NetworkResponse.Success(DataResponse(searchHolder!!))
        }
    }

    private fun getContentList(userList: UserList) = when(contentType){
        Constants.ContentType.ANIME -> userList.animeList
        Constants.ContentType.MOVIE -> userList.movieList
        Constants.ContentType.TV -> userList.tvList
        Constants.ContentType.GAME -> userList.gameList
    }

    fun setContentType(newContentType: Constants.ContentType) {
        if (newContentType != contentType) {
            contentType = newContentType
            savedStateHandle[USER_LIST_CONTENT_TYPE_KEY] = contentType

            setScrollPosition(0)
            resetSearch()
        }
    }

    fun setSearch(newSearch: String?) {
        search = newSearch
        savedStateHandle[USER_LIST_SEARCH_KEY] = search
    }
}