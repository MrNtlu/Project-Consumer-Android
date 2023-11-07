package com.mrntlu.projectconsumer.viewmodels.main.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.Preview
import com.mrntlu.projectconsumer.repository.PreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val previewRepository: PreviewRepository,
): ViewModel() {

    private val _previewList = MutableLiveData<NetworkResponse<Preview>>()
    val previewList: LiveData<NetworkResponse<Preview>> = _previewList

    init {
        getPreview()
    }

    fun getPreview() = networkResponseFlowCollector(
        previewRepository.getPreview()
    ) { response ->
        _previewList.value = response
    }
}