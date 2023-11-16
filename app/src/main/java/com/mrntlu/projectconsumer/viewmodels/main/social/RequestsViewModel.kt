package com.mrntlu.projectconsumer.viewmodels.main.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.auth.FriendRequest
import com.mrntlu.projectconsumer.models.auth.retrofit.AnswerFriendRequestBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUsernameBody
import com.mrntlu.projectconsumer.models.common.retrofit.DataNullableResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val REQUEST_SCROLL_POSITION_KEY = "rv.request.scroll_position"

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    private val _requestList = MutableLiveData<NetworkResponse<DataNullableResponse<List<FriendRequest>>>>()
    val requestList: LiveData<NetworkResponse<DataNullableResponse<List<FriendRequest>>>> = _requestList

    var scrollPosition: Int = savedStateHandle[REQUEST_SCROLL_POSITION_KEY] ?: 0
        private set

    fun getFriendRequests() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getFriendRequests().collect() { response ->
                withContext(Dispatchers.Main) {
                    _requestList.value = response
                }
            }
        }
    }

    fun answerFriendRequest(body: AnswerFriendRequestBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            userRepository.answerFriendRequest(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun setScrollPosition(newPosition: Int) {
        scrollPosition = newPosition
        savedStateHandle[REQUEST_SCROLL_POSITION_KEY] = scrollPosition
    }
}