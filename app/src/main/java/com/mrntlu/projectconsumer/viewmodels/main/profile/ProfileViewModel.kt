package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val userInteractionRepository: UserInteractionRepository,
): ViewModel() {
    var didOrientationChange = false
    private var currentOrientation: Orientation = Orientation.Idle

    fun setNewOrientation(newOrientation: Orientation) {
        if (currentOrientation == Orientation.Idle) {
            currentOrientation = newOrientation
        } else if (newOrientation != currentOrientation) {
            didOrientationChange = true

            currentOrientation = newOrientation
        }
    }

    private val _userInfoResponse = MutableLiveData<NetworkResponse<DataResponse<UserInfo>>>()
    val userInfoResponse: LiveData<NetworkResponse<DataResponse<UserInfo>>> = _userInfoResponse

    fun getUserInfo() = networkResponseFlowCollector(
        repository.getUserInfo()
    ) { response ->
        _userInfoResponse.value = response
    }

    fun deleteConsumeLater(body: IDBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.deleteConsumeLater(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}