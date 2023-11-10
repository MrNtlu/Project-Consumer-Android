package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileDisplayViewModel @Inject constructor(
    private val repository: UserRepository,
): ViewModel() {
    private val _userInfoResponse = MutableLiveData<NetworkResponse<DataResponse<UserInfo>>>()
    val userProfileResponse: LiveData<NetworkResponse<DataResponse<UserInfo>>> = _userInfoResponse

    fun getUserProfile(username: String) = networkResponseFlowCollector(
        repository.getUserProfile(username)
    ) { response ->
        _userInfoResponse.value = response
    }
}