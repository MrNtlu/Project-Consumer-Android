package com.mrntlu.projectconsumer.viewmodels.main.tv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateTVWatchListBody
import com.mrntlu.projectconsumer.repository.TVRepository
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TVDetailsViewModel @Inject constructor(
    private val repository: UserListRepository,
    private val userInteractionRepository: UserInteractionRepository,
    private val tvRepository: TVRepository,
): ViewModel() {
    private val _tvWatchList = MutableLiveData<NetworkResponse<DataResponse<TVSeriesWatchList>>>()
    val tvWatchList: LiveData<NetworkResponse<DataResponse<TVSeriesWatchList>>> = _tvWatchList

    private val _consumeLater = MutableLiveData<NetworkResponse<DataResponse<ConsumeLater>>>()
    val consumeLater: LiveData<NetworkResponse<DataResponse<ConsumeLater>>> = _consumeLater

    private val _tvDetails = MutableLiveData<NetworkResponse<DataResponse<TVSeriesDetails>>>()
    val tvDetails: LiveData<NetworkResponse<DataResponse<TVSeriesDetails>>> = _tvDetails

    fun createTVWatchList(body: TVWatchListBody) = networkResponseFlowCollector(
        repository.createTVWatchList(body)
    ) { response ->
        _tvWatchList.value = response
    }

    fun updateTVWatchList(body: UpdateTVWatchListBody) = networkResponseFlowCollector(
        repository.updateTVWatchList(body)
    ) { response ->
        _tvWatchList.value = response
    }

    fun deleteUserList(body: DeleteUserListBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserList(body).collect { response ->
                withContext(Dispatchers.Main) {
                    liveData.value = response
                }
            }
        }

        return liveData
    }

    fun getTVDetails(id: String) = networkResponseFlowCollector(
        tvRepository.getTVSeriesDetails(id)
    ) { response ->
        _tvDetails.value = response
    }
}