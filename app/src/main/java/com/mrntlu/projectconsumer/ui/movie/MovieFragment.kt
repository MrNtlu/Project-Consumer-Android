package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.viewmodels.main.movie.MoviePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment: BasePreviewFragment<Movie>() {

    private val viewModel: MoviePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.extraRVTV.text = getString(R.string.in_theaters)
        setHomeCardView()

        setGuidelineHeight()
        setListeners()
        setShowcaseRecyclerView(
            onItemClicked = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            onRefreshPressed = { viewModel.fetchPreviewMovies() }
        )
        setRecyclerView(
            firstOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewMovies() },
            secondOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewMovies() },
            extraOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            extraOnRefreshPressed = { viewModel.fetchPreviewMovies() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            seeAllButtonPopular.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.POPULAR.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonFirst.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.UPCOMING.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonSecond.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.TOP.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonExtra.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.EXTRA.tag)

                    navController.navigate(navWithAction)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.previewList.observe(viewLifecycleOwner) { handleObserver(it) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (
                (upcomingAdapter?.errorMessage != null && topRatedAdapter?.errorMessage != null && extraAdapter?.errorMessage != null) && it
            ) {
                viewModel.fetchPreviewMovies()
            }
        }
    }

    override fun onDestroyView() {
        viewModel.previewList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}