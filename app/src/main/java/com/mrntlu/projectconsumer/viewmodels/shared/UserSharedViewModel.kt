package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.auth.User
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserSharedViewModel @Inject constructor(
    private val repository: UserRepository,
): ViewModel() {
    var userInfo: User? = null

    private val _userInfoResponse = MutableLiveData<NetworkResponse<DataResponse<User>>>()
    val userInfoResponse: LiveData<NetworkResponse<DataResponse<User>>> = _userInfoResponse

    fun getUserInfo() = networkResponseFlowCollector(
        repository.getUserInfo()
    ) { response ->
        _userInfoResponse.value = response
    }
}