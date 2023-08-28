package com.mrntlu.projectconsumer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.MarkConsumeLaterBody
import com.mrntlu.projectconsumer.repository.UserInteractionRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
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

const val CONSUME_LATER_SEARCH_KEY = "rv.cl.search"

@HiltViewModel
class ConsumeLaterViewModel @Inject constructor(
    private val userInteractionRepository: UserInteractionRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {
    // Variable for detecting orientation change
    var didOrientationChange = false
    private var currentOrientation: Orientation = Orientation.Idle

    fun setNewOrientation(newOrientation: Orientation) {
        if (currentOrientation == Orientation.Idle) {
            currentOrientation = newOrientation
        } else if (newOrientation != currentOrientation) {
            didOrientationChange = true

            currentOrientation = newOrientation
        }
    }

    private val _consumeLaterList = MutableLiveData<NetworkListResponse<List<ConsumeLaterResponse>>>()
    val consumeLaterList: LiveData<NetworkListResponse<List<ConsumeLaterResponse>>> = _consumeLaterList

    private var searchHolder: ArrayList<ConsumeLaterResponse>? = null
    var search: String? = savedStateHandle[CONSUME_LATER_SEARCH_KEY]
        private set

    // Process Death variables
    var isRestoringData = false
    var sort: String = savedStateHandle[CONSUME_LATER_SORT_KEY] ?: Constants.SortConsumeLaterRequests[0].request
        private set
    var filter: String? = savedStateHandle[CONSUME_LATER_FILTER_KEY]
        private set
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

    fun moveConsumeLaterAsUserList(body: MarkConsumeLaterBody): LiveData<NetworkResponse<MessageResponse>> {
        val liveData = MutableLiveData<NetworkResponse<MessageResponse>>()

        viewModelScope.launch(Dispatchers.IO) {
            userInteractionRepository.moveConsumeLaterAsUserList(body).collect { response ->
                withContext(Dispatchers.Main) {
                    if (response is NetworkResponse.Success && searchHolder?.isNotEmpty() == true) {
                        val index = searchHolder!!.indexOfFirst {
                            it.id == body.id
                        }

                        if (index >= 0)
                            searchHolder!!.removeAt(index)
                    }

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
                    if (response is NetworkResponse.Success && searchHolder?.isNotEmpty() == true) {
                        val index = searchHolder!!.indexOfFirst {
                            it.id == body.id
                        }

                        if (index >= 0)
                            searchHolder!!.removeAt(index)
                    }

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

    fun setSort(newSort: String) {
        if (sort != newSort) {
            sort = newSort
            savedStateHandle[CONSUME_LATER_SORT_KEY] = sort

            resetSearch()
        }
    }

    fun setFilter(newFilter: String?) {
        if (filter != newFilter) {
            filter = newFilter
            savedStateHandle[CONSUME_LATER_FILTER_KEY] = filter

            resetSearch()
        }
    }

    fun setScrollPosition(newPosition: Int) {
        if (!isRestoringData && !didOrientationChange) {
            scrollPosition = newPosition
            savedStateHandle[CONSUME_LATER_SCROLL_POSITION_KEY] = scrollPosition
        }
    }

    fun search(search: String?) {
        setSearch(search)

        if (search != null && search.isNotEmptyOrBlank() && consumeLaterList.value?.isSuccessful() == true) {
            val data = consumeLaterList.value!!.data!!

            val currentData = if (
                searchHolder != null &&
                searchHolder!!.size > data.size
            ) {
                searchHolder!!.toMutableList().toCollection(ArrayList())
            } else {
                searchHolder = data.toMutableList().toCollection(ArrayList())

                data.toMutableList().toCollection(ArrayList())
            }

            var searchList = currentData.filter {
                it.content.titleOriginal.startsWith(search, ignoreCase = true) ||
                it.content.titleOriginal.contains(search, ignoreCase = true) ||
                it.content.titleEn.startsWith(search, ignoreCase = true) ||
                it.content.titleEn.contains(search, ignoreCase = true)
            }

            if (filter != null)
                searchList = searchList.filter {
                    it.contentType == filter
                }

            searchList = searchList.toMutableList().toCollection(ArrayList())

            currentData.clear()
            currentData.addAll(searchList)

            _consumeLaterList.value = NetworkListResponse(
                data = currentData,
                isLoading = false,
                isPaginating = false,
                isFailed = false,
                isPaginationData = false,
                isPaginationExhausted = true,
                errorMessage = null
            )
        } else if ((search.isNullOrEmpty() || search.isBlank()) && filter != null && searchHolder != null) {
            val currentData = searchHolder!!.toMutableList().toCollection(ArrayList())

            val searchList = currentData.filter {
                it.contentType == filter
            }.toMutableList().toCollection(ArrayList())

            _consumeLaterList.value = NetworkListResponse(
                data = searchList,
                isLoading = false,
                isPaginating = false,
                isFailed = false,
                isPaginationData = false,
                isPaginationExhausted = true,
                errorMessage = null
            )
        } else
            resetSearch()
    }

    private fun resetSearch() {
        if (search != null) {
            setSearch(null)
            _consumeLaterList.value = NetworkListResponse(
                data = searchHolder,
                isLoading = false,
                isPaginating = false,
                isFailed = false,
                isPaginationData = false,
                isPaginationExhausted = true,
                errorMessage = null
            )
        }
    }

    fun setSearch(newSearch: String?) {
        search = newSearch
        savedStateHandle[CONSUME_LATER_SEARCH_KEY] = search
    }
}