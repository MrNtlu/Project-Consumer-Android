package com.mrntlu.projectconsumer.viewmodels.main.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.models.common.retrofit.DataResponse
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.repository.AnimePreviewRepository
import com.mrntlu.projectconsumer.repository.TVPreviewRepository
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.networkResponseFlowCollector
import com.mrntlu.projectconsumer.viewmodels.main.profile.USER_LIST_CONTENT_TYPE_KEY
import com.mrntlu.projectconsumer.viewmodels.main.profile.USER_LIST_SCROLL_POSITION_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

const val CURRENT_WEEK_DAY = "rv.dow.week_day"
const val SCROLL_POSITION_KEY = "rv.dow.scroll_position"

@HiltViewModel
class DayOfWeekViewModel @Inject constructor(
    private val tvRepository: TVPreviewRepository,
    private val animeRepository: AnimePreviewRepository,
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    var selectedDayOfWeek: Int = savedStateHandle[CURRENT_WEEK_DAY] ?: LocalDate.now().dayOfWeek.value
        private set
    var scrollPosition: Int = savedStateHandle[USER_LIST_SCROLL_POSITION_KEY] ?: 0
        private set

    private val _tvList = MutableLiveData<NetworkResponse<DataResponse<TVSeries>>>()
    val tvList: LiveData<NetworkResponse<DataResponse<TVSeries>>> = _tvList

    private val _animeList = MutableLiveData<NetworkResponse<DataResponse<Anime>>>()
    val animeList: LiveData<NetworkResponse<DataResponse<Anime>>> = _animeList

    fun getAnimeDayOfWeek() = networkResponseFlowCollector(
        animeRepository.getCurrentlyAiringTVSeriesByDayOfWeek()
    ) { response ->
        _animeList.value = response
    }

    fun getTVSeriesDayOfWeek() = networkResponseFlowCollector(
        tvRepository.getCurrentlyAiringTVSeriesByDayOfWeek()
    ) { response ->
        _tvList.value = response
    }

    fun setSelectedDayOfWeek(newDayOfWeek: Int) {
        if (newDayOfWeek != selectedDayOfWeek) {
            selectedDayOfWeek = newDayOfWeek
            savedStateHandle[USER_LIST_CONTENT_TYPE_KEY] = selectedDayOfWeek

            setScrollPosition(0)
        }
    }

    fun setScrollPosition(newPosition: Int) {
        scrollPosition = newPosition
        savedStateHandle[USER_LIST_SCROLL_POSITION_KEY] = scrollPosition
    }
}