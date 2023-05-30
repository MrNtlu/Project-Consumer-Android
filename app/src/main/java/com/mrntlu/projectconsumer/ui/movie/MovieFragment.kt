package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.main.movie.MoviePreviewViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ViewPagerSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment: BaseFragment<FragmentMovieBinding>() {

    private val viewModel: MoviePreviewViewModel by viewModels()
    private val viewPagerSharedViewModel: ViewPagerSharedViewModel by activityViewModels()

    private var upcomingAdapter: PreviewAdapter<Movie>? = null
    private var popularAdapter: PreviewAdapter<Movie>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setRecyclerView()
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            movieScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                viewPagerSharedViewModel.setScrollYPosition(scrollY)
            }

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationMovieToMovieListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationMovieToMovieListFragment(FetchType.POPULAR.tag)

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setRecyclerView() {
        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(object: Interaction<Movie> {
                override fun onItemSelected(item: Movie, position: Int) {
                    val navWithAction = HomeFragmentDirections.actionNavigationMovieToMovieDetailsFragment(item.id)

                    navController.navigate(navWithAction)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onErrorRefreshPressed() {
                    viewModel.fetchUpcomingMovies()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = upcomingAdapter
        }

        binding.popularPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            popularAdapter = PreviewAdapter(object: Interaction<Movie> {
                override fun onItemSelected(item: Movie, position: Int) {
                    val navWithAction = HomeFragmentDirections.actionNavigationMovieToMovieDetailsFragment(item.id)

                    navController.navigate(navWithAction)
                }

                override fun onErrorRefreshPressed() {
                    viewModel.fetchPopularMovies()
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = popularAdapter
        }
    }

    private fun setObservers() {
        viewModel.upcomingMovies.observe(viewLifecycleOwner) { handleObserver(it, upcomingAdapter) }

        viewModel.popularMovies.observe(viewLifecycleOwner) { handleObserver(it, popularAdapter) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (upcomingAdapter?.errorMessage != null && it) {
                viewModel.fetchUpcomingMovies()
            }

            if (popularAdapter?.errorMessage != null && it) {
                viewModel.fetchPopularMovies()
            }
        }
    }

    private fun handleObserver(response: NetworkResponse<DataPaginationResponse<Movie>>, adapter: PreviewAdapter<Movie>?) {
        when(response) {
            is NetworkResponse.Failure -> adapter?.setErrorView(response.errorMessage)
            NetworkResponse.Loading -> adapter?.setLoadingView()
            is NetworkResponse.Success -> {
                adapter?.setData(response.data.data)
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.upcomingMovies.removeObservers(this)
            viewModel.popularMovies.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }
        binding.movieScrollView.setOnScrollChangeListener(null)
        upcomingAdapter = null
        popularAdapter = null
        super.onDestroyView()
    }
}