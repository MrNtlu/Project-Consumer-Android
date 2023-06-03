package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.viewmodels.main.movie.MoviePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem


@AndroidEntryPoint
class MovieFragment: BasePreviewFragment<Movie>() {

    private val viewModel: MoviePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
//        setViewPager()
        setLoadingViewPager()
        setRecyclerView(
            firstOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            firstOnRefreshPressed = { viewModel.fetchUpcomingMovies() },
            secondOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            secondOnRefreshPressed = { viewModel.fetchPopularMovies() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            setScrollListener()

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieListFragment(FetchType.POPULAR.tag)

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setObservers() {
        viewModel.upcomingMovies.observe(viewLifecycleOwner) { handleObserver(it, upcomingAdapter) }

        viewModel.popularMovies.observe(viewLifecycleOwner) {
            binding.loadingViewPager.setVisibilityByCondition(it != NetworkResponse.Loading)

            //TODO Error layout

            when(it) {
                is NetworkResponse.Success -> {
                    setViewPager(it.data.data.map { movie ->
                        val headers = mutableMapOf<String, String>()
                        headers["id"] = movie.id

                        CarouselItem(
                            imageUrl = movie.imageURL,
                            caption = movie.title,
                            headers = headers
                        )
                    }) { id ->
                        if (id != null) {
                            val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)

                            navController.navigate(navWithAction)
                        }
                    }
                }
                is NetworkResponse.Failure -> {
                    binding.previewViewPager.setGone()
                }
                else -> {}
            }

            handleObserver(it, popularAdapter)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (upcomingAdapter?.errorMessage != null && it) {
                viewModel.fetchUpcomingMovies()
            }

            if (popularAdapter?.errorMessage != null && it) {
                viewModel.fetchPopularMovies()
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.upcomingMovies.removeObservers(this)
            viewModel.popularMovies.removeObservers(this)
        }
        super.onDestroyView()
    }
}