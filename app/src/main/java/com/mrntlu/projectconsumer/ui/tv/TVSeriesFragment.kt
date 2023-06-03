package com.mrntlu.projectconsumer.ui.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVPreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesFragment : BasePreviewFragment<TVSeries>() {

    private val viewModel: TVPreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setRecyclerView(
            firstOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            firstOnRefreshPressed = { viewModel.fetchUpcomingTVSeries() },
            secondOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            secondOnRefreshPressed = { viewModel.fetchPopularTVSeries() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            setScrollListener()

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToTvListFragment(FetchType.POPULAR.tag)

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setObservers() {
        viewModel.upcomingTV.observe(viewLifecycleOwner) { handleObserver(it, upcomingAdapter) }

        viewModel.popularTV.observe(viewLifecycleOwner) { handleObserver(it, popularAdapter) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (upcomingAdapter?.errorMessage != null && it) {
                viewModel.fetchUpcomingTVSeries()
            }

            if (popularAdapter?.errorMessage != null && it) {
                viewModel.fetchPopularTVSeries()
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.upcomingTV.removeObservers(this)
            viewModel.popularTV.removeObservers(this)
        }
        super.onDestroyView()
    }
}