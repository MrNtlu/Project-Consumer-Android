package com.mrntlu.projectconsumer.viewmodels.main.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AISuggestionsConsumeLaterViewModel @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository,
): ViewModel() {
    fun createConsumeLater(body: ConsumeLaterBody): LiveData<NetworkResponse<DataResponse<ConsumeLater>>> {
        val liveData = MutableLiveData<NetworkResponse<DataResponse<ConsumeLater>>>()

        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.createConsumeLater(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
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