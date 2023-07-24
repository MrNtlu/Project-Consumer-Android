package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.models.main.userList.convertToAnimeList
import com.mrntlu.projectconsumer.models.main.userList.convertToGameList
import com.mrntlu.projectconsumer.models.main.userList.convertToMovieList
import com.mrntlu.projectconsumer.models.main.userList.convertToTVSeriesList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
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

    val totalYScroll: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setTotalYPosition(dy: Int) {
        val newTotal = this.totalYScroll.value?.plus(dy)
        this.totalYScroll.value = if (newTotal != null) {
            if (newTotal > 500) 501
            else if (newTotal < 0) 0
            else newTotal
        } else null
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

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        getUserList()
    }

    fun getUserList() = networkResponseFlowCollector(
        repository.getUserList(sort)
    ) { response ->
        _userList.value = response
    }

    fun deleteUserList(body: DeleteUserListBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserList(body).collect { response ->
                withContext(Dispatchers.Main) {
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

            resetSearch()
        }
    }

    fun setSearch(newSearch: String?) {
        search = newSearch
        savedStateHandle[USER_LIST_SEARCH_KEY] = search
    }
}