package com.mrntlu.projectconsumer.viewmodels.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.userList.LogsByDate
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: UserListRepository,
): ViewModel() {
    // Scroll Y
    val totalYScroll: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setTotalYPosition(dy: Int) {
        val newTotal = this.totalYScroll.value?.plus(dy)
        this.totalYScroll.value = if (newTotal != null) {
            if (newTotal > 800) 801
            else if (newTotal < 0) 0
            else newTotal
        } else null
    }

    private val _logsResponse = MutableLiveData<NetworkResponse<DataResponse<List<LogsByDate>?>>>()
    val logsResponse: LiveData<NetworkResponse<DataResponse<List<LogsByDate>?>>> = _logsResponse

    fun getUserLogs(from: String, to: String) = networkResponseFlowCollector(
        repository.getUserLogs(from, to)
    ) { response ->
        _logsResponse.value = response
    }
}