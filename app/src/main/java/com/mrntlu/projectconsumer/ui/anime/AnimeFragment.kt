package com.mrntlu.projectconsumer.ui.anime

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.viewmodels.main.anime.AnimePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeFragment : BasePreviewFragment<Anime>() {

    private val viewModel: AnimePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGuidelineHeight()
        setListeners()
        setShowcaseRecyclerView(
            onItemClicked = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                    navController.navigate(navWithAction)
                }

            },
            onRefreshPressed = { viewModel.fetchPreviewAnimes() }
        )
        setRecyclerView(
            firstOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewAnimes() },
            secondOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewAnimes() },
            extraOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            extraOnRefreshPressed = { viewModel.fetchPreviewAnimes() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            seeAllButtonPopular.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(FetchType.POPULAR.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonFirst.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(FetchType.UPCOMING.tag)

                    navController.navigate(navWithAction)
                }

            }

            seeAllButtonSecond.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(FetchType.TOP.tag)
                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonExtra.setOnClickListener {
//                if (navController.currentDestination?.id == R.id.navigation_home) {
//                    TODO Navigate to DayOfWeekList fragment
//                }
            }
        }
    }

    private fun setObservers() {
        viewModel.previewList.observe(viewLifecycleOwner) { handleObserver(it) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (
                (upcomingAdapter?.errorMessage != null && topRatedAdapter?.errorMessage != null && extraAdapter?.errorMessage != null) && it
            ) {
                viewModel.fetchPreviewAnimes()
            }
        }
    }

    override fun onDestroyView() {
        viewModel.previewList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}