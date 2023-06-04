package com.mrntlu.projectconsumer.viewmodels.main.tv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.repository.TVPreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TVPreviewViewModel @Inject constructor(
    private val tvPreviewRepository: TVPreviewRepository
): ViewModel() {
    private val _previewList = MutableLiveData<NetworkResponse<PreviewResponse<TVSeries>>>()
    val previewList: LiveData<NetworkResponse<PreviewResponse<TVSeries>>> = _previewList

    init {
        fetchPreviewTVSeries()
    }

    fun fetchPreviewTVSeries() = networkResponseFlowCollector(
        tvPreviewRepository.fetchPreviewTVSeries()
    ) { response ->
        _previewList.value = response
    }
}