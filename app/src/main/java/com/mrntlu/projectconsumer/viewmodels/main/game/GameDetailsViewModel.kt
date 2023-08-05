package com.mrntlu.projectconsumer.viewmodels.main.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.game.GameDetails
import com.mrntlu.projectconsumer.repository.GameRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameDetailsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
): ViewModel() {
    private val _gameDetails = MutableLiveData<NetworkResponse<DataResponse<GameDetails>>>()
    val gameDetails: LiveData<NetworkResponse<DataResponse<GameDetails>>> = _gameDetails

    fun getGameDetails(id: String) = networkResponseFlowCollector(
        gameRepository.getGameDetails(id)
    ) { response ->
        _gameDetails.value = response
    }
}