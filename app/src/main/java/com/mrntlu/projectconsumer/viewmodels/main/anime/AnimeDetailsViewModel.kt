package com.mrntlu.projectconsumer.viewmodels.main.anime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import com.mrntlu.projectconsumer.repository.AnimeRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
): ViewModel() {
    private val _animeDetails = MutableLiveData<NetworkResponse<DataResponse<AnimeDetails>>>()
    val animeDetails: LiveData<NetworkResponse<DataResponse<AnimeDetails>>> = _animeDetails

    fun getAnimeDetails(id: String) = networkResponseFlowCollector(
        animeRepository.getAnimeDetails(id)
    ) { response ->
        _animeDetails.value = response
    }
}