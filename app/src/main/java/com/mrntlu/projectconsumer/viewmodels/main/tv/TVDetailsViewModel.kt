package com.mrntlu.projectconsumer.viewmodels.main.tv

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import com.mrntlu.projectconsumer.repository.TVRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TVDetailsViewModel @Inject constructor(
    private val tvRepository: TVRepository,
): ViewModel() {
    private val _tvDetails = MutableLiveData<NetworkResponse<DataResponse<TVSeriesDetails>>>()
    val tvDetails: LiveData<NetworkResponse<DataResponse<TVSeriesDetails>>> = _tvDetails

    fun getTVDetails(id: String) = networkResponseFlowCollector(
        tvRepository.getTVSeriesDetails(id)
    ) { response ->
        _tvDetails.value = response
    }
}