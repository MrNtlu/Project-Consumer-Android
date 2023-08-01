package com.mrntlu.projectconsumer.viewmodels.main.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.repository.GamePreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GamePreviewViewModel @Inject constructor(
    private val gamePreviewRepository: GamePreviewRepository,
): ViewModel() {
    private val _previewList = MutableLiveData<NetworkResponse<PreviewResponse<Game>>>()
    val previewList: LiveData<NetworkResponse<PreviewResponse<Game>>> = _previewList

    init {
        fetchPreviewGames()
    }

    fun fetchPreviewGames() = networkResponseFlowCollector(
        gamePreviewRepository.fetchPreviewGames()
    ) { response ->
        _previewList.value = response
    }
}