package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateFCMTokenBody
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateUserImageBody
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserSharedViewModel @Inject constructor(
    private val repository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
): ViewModel() {
    var userInfo: BasicUserInfo? = null

    private val _userInfoResponse = MutableLiveData<NetworkResponse<DataResponse<BasicUserInfo>>>()
    val userInfoResponse: LiveData<NetworkResponse<DataResponse<BasicUserInfo>>> = _userInfoResponse

    fun getBasicInfo() = networkResponseFlowCollector(
        repository.getBasicUserInfo()
    ) { response ->
        if (response is NetworkResponse.Success) {
            firebaseMessaging.token.addOnCompleteListener {
                if (it.result != response.data.data.fcmToken) {
                    updateFCMToken(UpdateFCMTokenBody(it.result))
                }
            }
        }

        _userInfoResponse.value = response
    }

    private fun updateFCMToken(body: UpdateFCMTokenBody) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFCMToken(body).collect()
        }
    }

    fun updateUserImage(image: UpdateUserImageBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserImage(image).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}