package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.service.retrofit.GameApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class GamePreviewRepository @Inject constructor(
  private val gameApiService: GameApiService,
) {
    fun fetchPreviewGames() = networkResponseFlow {
        gameApiService.getPreviewGames()
    }
}