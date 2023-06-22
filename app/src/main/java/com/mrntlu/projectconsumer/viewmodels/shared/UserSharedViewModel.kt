package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
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
    var userInfo: BasicUserInfo? = null

    private val _userInfoResponse = MutableLiveData<NetworkResponse<DataResponse<BasicUserInfo>>>()
    val userInfoResponse: LiveData<NetworkResponse<DataResponse<BasicUserInfo>>> = _userInfoResponse

    fun getBasicInfo() = networkResponseFlowCollector(
        repository.getBasicUserInfo()
    ) { response ->
        _userInfoResponse.value = response
    }
}