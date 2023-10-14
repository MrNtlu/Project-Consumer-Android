package com.mrntlu.projectconsumer.ui.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVPreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesFragment : BasePreviewFragment<TVSeries>() {

    private val viewModel: TVPreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGuidelineHeight()
        setListeners()
        setShowcaseRecyclerView(
            onItemClicked = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            onRefreshPressed = { viewModel.fetchPreviewTVSeries() }
        )
        setRecyclerView(
            firstOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewTVSeries() },
            secondOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewTVSeries() },
            extraOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            extraOnRefreshPressed = { viewModel.fetchPreviewTVSeries() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            seeAllButtonPopular.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvListFragment(FetchType.POPULAR.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonFirst.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvListFragment(FetchType.UPCOMING.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonSecond.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvListFragment(FetchType.TOP.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonExtra.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    navController.navigate(R.id.action_navigation_home_to_tvSeriesDayOfWeekFragment)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.previewList.observe(viewLifecycleOwner) { handleObserver(it) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (
                (upcomingAdapter?.errorMessage != null || topRatedAdapter?.errorMessage != null && extraAdapter?.errorMessage != null) && it
            ) {
                viewModel.fetchPreviewTVSeries()
            }
        }
    }

    override fun onDestroyView() {
        viewModel.previewList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}