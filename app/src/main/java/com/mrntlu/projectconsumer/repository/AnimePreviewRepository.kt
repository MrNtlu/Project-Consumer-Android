package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.AnimeApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class AnimePreviewRepository @Inject constructor(
    private val animeApiService: AnimeApiService,
) {
    fun fetchPreviewAnimes() = networkResponseFlow {
        animeApiService.getPreviewAnimes()
    }

    fun getCurrentlyAiringTVSeriesByDayOfWeek() = networkResponseFlow {
        animeApiService.getCurrentlyAiringAnimesByDayOfWeek()
    }
}