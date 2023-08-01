package com.mrntlu.projectconsumer.viewmodels.main.anime

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.repository.AnimePreviewRepository
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnimePreviewViewModel @Inject constructor(
    private val animePreviewRepository: AnimePreviewRepository,
): ViewModel() {
    private val _previewList = MutableLiveData<NetworkResponse<PreviewResponse<Anime>>>()
    val previewList: LiveData<NetworkResponse<PreviewResponse<Anime>>> = _previewList

    init {
        fetchPreviewAnimes()
    }

    fun fetchPreviewAnimes() = networkResponseFlowCollector(
        animePreviewRepository.fetchPreviewAnimes()
    ) { response ->
        _previewList.value = response
    }
}