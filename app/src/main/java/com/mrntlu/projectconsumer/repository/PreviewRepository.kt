package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.PreviewApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class PreviewRepository @Inject constructor(
    private val previewApiService: PreviewApiService,
) {
    fun getPreview() = networkResponseFlow {
        previewApiService.getPreview()
    }
}