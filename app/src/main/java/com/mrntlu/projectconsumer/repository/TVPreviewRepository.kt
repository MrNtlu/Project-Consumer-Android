package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class TVPreviewRepository @Inject constructor(
    private val tvSeriesApiService: TVSeriesApiService,
) {

    fun fetchPreviewTVSeries() = networkResponseFlow {
        tvSeriesApiService.getPreviewTVSeries()
    }
}