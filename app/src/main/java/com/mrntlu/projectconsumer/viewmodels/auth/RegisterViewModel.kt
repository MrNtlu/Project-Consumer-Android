package com.mrntlu.projectconsumer.viewmodels.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.auth.retrofit.RegisterBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.repository.AuthRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
): ViewModel() {

    private val _registerResponse = MutableLiveData<NetworkResponse<MessageResponse>>()
    val registerResponse: LiveData<NetworkResponse<MessageResponse>> = _registerResponse

    fun register(body: RegisterBody) = networkResponseFlowCollector(
        repository.register(body)
    ) { response ->
        _registerResponse.value = response
    }
}