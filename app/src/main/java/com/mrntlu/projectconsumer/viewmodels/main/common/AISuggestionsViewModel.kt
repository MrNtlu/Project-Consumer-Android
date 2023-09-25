package com.mrntlu.projectconsumer.viewmodels.main.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.AISuggestionResponse
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.repository.AISuggestionsRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AISuggestionsViewModel @Inject constructor(
    private val repository: AISuggestionsRepository,
): ViewModel() {

    init {
        getAISuggestions()
    }

    private val _aiSuggestionsResponse = MutableLiveData<NetworkResponse<DataResponse<AISuggestionResponse>>>()
    val aiSuggestionsResponse: LiveData<NetworkResponse<DataResponse<AISuggestionResponse>>> = _aiSuggestionsResponse

    fun getAISuggestions() = networkResponseFlowCollector(
        repository.getAISuggestions()
    ) { response ->
        _aiSuggestionsResponse.value = response
    }
}