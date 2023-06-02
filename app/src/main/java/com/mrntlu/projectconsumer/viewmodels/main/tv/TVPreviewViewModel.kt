package com.mrntlu.projectconsumer.viewmodels.main.tv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
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
    private val _upcomingList = MutableLiveData<NetworkResponse<DataPaginationResponse<TVSeries>>>()
    val upcomingTV: LiveData<NetworkResponse<DataPaginationResponse<TVSeries>>> = _upcomingList

    private val _popularList = MutableLiveData<NetworkResponse<DataPaginationResponse<TVSeries>>>()
    val popularTV: LiveData<NetworkResponse<DataPaginationResponse<TVSeries>>> = _popularList

    init {
        fetchUpcomingTVSeries()
        fetchPopularTVSeries()
    }

    fun fetchUpcomingTVSeries() = networkResponseFlowCollector(
        tvPreviewRepository.fetchUpcomingTVSeries()
    ) { response ->
        _upcomingList.value = response
    }

    fun fetchPopularTVSeries() = networkResponseFlowCollector(
        tvPreviewRepository.fetchPopularTVSeries()
    ) { response ->
        _popularList.value = response
    }
}