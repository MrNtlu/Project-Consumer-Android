package com.mrntlu.projectconsumer.viewmodels.main.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val SOCIAL_SCROLL_POSITION_KEY = "rv.sh.scroll_position"

@HiltViewModel
class SocialHubViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val _friendList = MutableLiveData<NetworkResponse<DataResponse<List<BasicUserInfo>>>>()
    val friendList: LiveData<NetworkResponse<DataResponse<List<BasicUserInfo>>>> = _friendList

    var scrollPosition: Int = savedStateHandle[SOCIAL_SCROLL_POSITION_KEY] ?: 0
        private set

    fun getFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getFriends().collect() { response ->
                withContext(Dispatchers.Main) {
                    _friendList.value = response
                }
            }
        }
    }

    fun setScrollPosition(newPosition: Int) {
        scrollPosition = newPosition
        savedStateHandle[SOCIAL_SCROLL_POSITION_KEY] = scrollPosition
    }
}