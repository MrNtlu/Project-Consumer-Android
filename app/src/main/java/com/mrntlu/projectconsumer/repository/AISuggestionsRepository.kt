package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.AISuggestionApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class AISuggestionsRepository @Inject constructor(
    private val aiSuggestionApiService: AISuggestionApiService,
) {

    fun getAISuggestions() = networkResponseFlow {
        aiSuggestionApiService.getAISuggestions()
    }
}