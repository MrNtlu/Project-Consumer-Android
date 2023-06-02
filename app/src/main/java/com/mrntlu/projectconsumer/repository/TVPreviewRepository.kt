package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class TVPreviewRepository @Inject constructor(
    private val tvSeriesApiService: TVSeriesApiService,
) {

    fun fetchUpcomingTVSeries() = networkResponseFlow {
        tvSeriesApiService.getUpcomingTVSeries(1, "popularity")
    }

    fun fetchPopularTVSeries() = networkResponseFlow {
        tvSeriesApiService.getPopularTVSeries(1)
    }
}