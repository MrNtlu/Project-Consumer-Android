package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.ui.BaseDayOfWeekFragment
import com.mrntlu.projectconsumer.viewmodels.main.common.DayOfWeekViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeDayOfWeekFragment: BaseDayOfWeekFragment<Anime>(){

    private val viewModel: DayOfWeekViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}