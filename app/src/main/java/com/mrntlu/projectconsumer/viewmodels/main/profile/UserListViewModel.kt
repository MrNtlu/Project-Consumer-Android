package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.SortBody
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val USER_LIST_SORT_KEY = "rv.ul.sort"
const val USER_LIST_SCROLL_POSITION_KEY = "rv.ul.scroll_position"

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val repository: UserListRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val _userList = MutableLiveData<NetworkResponse<DataResponse<UserList>>>()
    val userList: LiveData<NetworkResponse<DataResponse<UserList>>> = _userList

    // Process Death variables
    var sort: String = savedStateHandle[USER_LIST_SORT_KEY] ?: Constants.SortUserListRequests[0].request
        private set
    var scrollPosition: Int = savedStateHandle[USER_LIST_SCROLL_POSITION_KEY] ?: 0
        private set

    // Variable for detecting orientation change
    var didOrientationChange = false

    init {
        getUserList(sort)
    }

    fun getUserList(sort: String) = networkResponseFlowCollector(
        repository.getUserList(SortBody(sort))
    ) { response ->
        _userList.value = response
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
}