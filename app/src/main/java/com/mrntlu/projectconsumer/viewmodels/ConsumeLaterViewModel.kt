package com.mrntlu.projectconsumer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.setData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val CONSUME_LATER_SORT_KEY = "rv.cl.sort"
const val CONSUME_LATER_SCROLL_POSITION_KEY = "rv.cl.scroll_position"
const val CONSUME_LATER_FILTER_KEY = "rv.cl.scroll_position"

@HiltViewModel
class ConsumeLaterViewModel @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel(){
    private val _consumeLaterList = MutableLiveData<NetworkListResponse<List<ConsumeLaterResponse>>>()
    val consumeLaterList: LiveData<NetworkListResponse<List<ConsumeLaterResponse>>> = _consumeLaterList

    // Process Death variables
    var isRestoringData = false

    // Variable for detecting orientation change
    var didOrientationChange = false

    private var sort: String = savedStateHandle[CONSUME_LATER_SORT_KEY] ?: Constants.SortConsumeLaterRequests[0].request
    private var filter: String? = savedStateHandle[CONSUME_LATER_FILTER_KEY]
    var scrollPosition: Int = savedStateHandle[CONSUME_LATER_SCROLL_POSITION_KEY] ?: 0
        private set

    init {
        if (scrollPosition != 0)
            restoreData()
        else
            getConsumeLater()
    }

    fun getConsumeLater() {
        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.getConsumeLater(filter, sort).collect { response ->
                withContext(Dispatchers.Main) {
                    _consumeLaterList.value = response
                }
            }
        }
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

    private fun restoreData() {
        isRestoringData = true

        val tempList = arrayListOf<ConsumeLaterResponse>()
        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.getConsumeLater(filter, sort, isRestoringData = true).collect { response ->
                if (response.isSuccessful()) {
                    tempList.addAll(response.data!!)
                }
            }

            withContext(Dispatchers.Main) {
                _consumeLaterList.value = setData(
                    tempList,
                    isPaginationData = false,
                    isPaginationExhausted = true
                )
            }
        }
    }

    private fun setSort(newSort: String) {
        sort = newSort
        savedStateHandle[CONSUME_LATER_SORT_KEY] = sort
    }

    private fun setFilter(newFilter: String?) {
        filter = newFilter
        savedStateHandle[CONSUME_LATER_FILTER_KEY] = filter
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[CONSUME_LATER_SCROLL_POSITION_KEY] = scrollPosition
        }
    }
}