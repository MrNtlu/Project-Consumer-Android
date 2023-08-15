package com.mrntlu.projectconsumer.viewmodels.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.auth.retrofit.ForgotPasswordBody
import com.mrntlu.projectconsumer.models.auth.retrofit.GoogleLoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.repository.AuthRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
): ViewModel() {

    private val _loginResponse = MutableLiveData<NetworkResponse<LoginResponse>>()
    val loginResponse: LiveData<NetworkResponse<LoginResponse>> = _loginResponse

    fun googleLogin(body: GoogleLoginBody) = networkResponseFlowCollector(
        repository.googleLogin(body)
    ) { response ->
        _loginResponse.value = response
    }

    fun login(body: LoginBody) = networkResponseFlowCollector(
        repository.login(body)
    ) { response ->
        _loginResponse.value = response
    }

    fun forgotPassword(body: ForgotPasswordBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.forgotPassword(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }
}