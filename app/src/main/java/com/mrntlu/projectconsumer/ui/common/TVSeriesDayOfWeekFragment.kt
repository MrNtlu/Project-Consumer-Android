package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.ui.BaseDayOfWeekFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.main.common.DayOfWeekViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesDayOfWeekFragment: BaseDayOfWeekFragment<TVSeries>() {

    private companion object {
        private const val KEY_VALUE = "day_of_week"
    }

    private val viewModel: DayOfWeekViewModel by viewModels()

    private var isNavigatingBack = false
    private var isInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            isInitialized = it.getBoolean(KEY_VALUE)
        }

        setToolbar()
        setUI()
        setObservers()
    }

    override fun onStart() {
        super.onStart()

        if (isNavigatingBack || isInitialized) {
            binding.userListTabLayout.tabLayout.apply {
                getTabAt(viewModel.selectedDayOfWeek)?.select()

                post {
                    val tabView = (getChildAt(0) as ViewGroup).getChildAt(viewModel.selectedDayOfWeek)
                    val scrollToX = tabView.left - (width - tabView.width) / 2
                    scrollTo(scrollToX, 0)
                }
            }

            isNavigatingBack = false
        }
    }

    private fun setObservers() {
        if (sharedViewModel.isAltLayout()) {
            gridCount = 1

            setRV()
            setListeners()
        } else {
            sharedViewModel.windowSize.observe(viewLifecycleOwner) {
                val widthSize: WindowSizeClass = it

                gridCount = when(widthSize) {
                    WindowSizeClass.COMPACT -> 2
                    WindowSizeClass.MEDIUM -> 3
                    WindowSizeClass.EXPANDED -> 5
                }

                setRV()
                setListeners()
            }
        }

        if (!(viewModel.tvList.hasObservers() || viewModel.tvList.value is NetworkResponse.Success || viewModel.tvList.value is NetworkResponse.Loading))
            viewModel.getTVSeriesDayOfWeek()

        viewModel.tvList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> contentAdapter?.setErrorView(response.errorMessage)
                NetworkResponse.Loading -> contentAdapter?.setLoadingView()
                is NetworkResponse.Success -> {
                    setDataWithFilter()
                }
            }
        }
    }

    private fun setDataWithFilter() {
        if (viewModel.tvList.value is NetworkResponse.Success) {
            isInitialized = true

            val response = viewModel.tvList.value as NetworkResponse.Success

            val dayOfWeekCode = when(viewModel.selectedDayOfWeek) {
                0 -> 2
                1 -> 3
                2 -> 4
                3 -> 5
                4 -> 6
                5 -> 7
                else -> 1
            }

            contentAdapter?.setData(
                response.data.data.first {
                    it.dayOfWeek == dayOfWeekCode
                }.data.toCollection(ArrayList()),
                isPaginationExhausted = true,
            )
        }
    }

    private fun setRV() {
        setRecyclerView(
            startFetch = { viewModel.getTVSeriesDayOfWeek() },
            onItemSelected = { itemId ->
                if (navController.currentDestination?.id == R.id.tvSeriesDayOfWeekFragment) {
                    isNavigatingBack = true

                    val navWithAction = TVSeriesDayOfWeekFragmentDirections.actionTvSeriesDayOfWeekFragmentToTvDetailsFragment(itemId)
                    navController.navigate(navWithAction)
                }
            },
        )
    }

    private fun setListeners() {
        binding.userListTabLayout.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewModel.setSelectedDayOfWeek(tab.position)
                    setDataWithFilter()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(KEY_VALUE, true)
    }

    override fun onDestroyView() {
        viewModel.tvList.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}