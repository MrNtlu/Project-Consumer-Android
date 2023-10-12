package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.ui.BaseDayOfWeekFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.main.common.DayOfWeekViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeDayOfWeekFragment: BaseDayOfWeekFragment<Anime>(){

    private val viewModel: DayOfWeekViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setObservers()
    }

    private fun setObservers() {
        if (sharedViewModel.isAltLayout()) {
            gridCount = 1

            setRV()
        } else {
            sharedViewModel.windowSize.observe(viewLifecycleOwner) {
                val widthSize: WindowSizeClass = it

                gridCount = when(widthSize) {
                    WindowSizeClass.COMPACT -> 2
                    WindowSizeClass.MEDIUM -> 3
                    WindowSizeClass.EXPANDED -> 5
                }

                setRV()
            }
        }

        if (!(viewModel.animeList.hasObservers() || viewModel.animeList.value is NetworkResponse.Success || viewModel.animeList.value is NetworkResponse.Loading))
            viewModel.getAnimeDayOfWeek()

        viewModel.animeList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> contentAdapter?.setErrorView(response.errorMessage)
                NetworkResponse.Loading -> contentAdapter?.setLoadingView()
                is NetworkResponse.Success -> {
                    printLog("${viewModel.selectedDayOfWeek}")
                    contentAdapter?.setData(
                        response.data.data.first {
                            it.dayOfWeek == viewModel.selectedDayOfWeek
                        }.data.toCollection(ArrayList()),
                        isPaginationExhausted = true,
                    )
                }
            }
        }
    }

    private fun setRV() {
        setRecyclerView(
            startFetch = { viewModel.getAnimeDayOfWeek() },
            onItemSelected = { itemId ->
                if (navController.currentDestination?.id == R.id.animeDayOfWeekFragment) {

                }
            },
            scrollViewModel = { position -> viewModel.setScrollPosition(position) },
        )
    }

    override fun onDestroyView() {
        viewModel.animeList.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}