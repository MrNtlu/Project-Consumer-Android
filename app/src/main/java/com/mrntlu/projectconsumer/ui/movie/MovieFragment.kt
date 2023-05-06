package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.MovieResponse
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.compose.GenreGrid
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.movie.MoviePreviewViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment: BaseFragment<FragmentMovieBinding>() {

    private val viewModel: MoviePreviewViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

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
        setGridLayout()
        setRecyclerView()
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            previewSearchView.apply {
                setOnClickListener {
                    isIconified = false
                    requestFocus()
                }
            }

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = MovieFragmentDirections.actionNavigationMovieToMovieListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = MovieFragmentDirections.actionNavigationMovieToMovieListFragment(FetchType.POPULAR.tag)

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setGridLayout() {
        binding.gridLayoutCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GenreGrid(
                    !sharedViewModel.isLightTheme(),
                    Constants.MovieGenreList,
                    onDiscoveryClicked = {
                        printLog("Discover")
                    },
                    onGenreClicked = {
                        printLog("Genre clicked $it")
                    }
                )
            }
        }
    }

    private fun setRecyclerView() {
        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(object: Interaction<Movie> {
                override fun onItemSelected(item: Movie, position: Int) {
                    val navWithAction = MovieFragmentDirections.actionNavigationMovieToMovieDetailsFragment(item)

                    navController.navigate(navWithAction)
                }

                override fun onErrorRefreshPressed() {
                    viewModel.fetchUpcomingMovies()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = upcomingAdapter
        }

        binding.popularPreviewRV.apply {
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            popularAdapter = PreviewAdapter(object: Interaction<Movie> {
                override fun onItemSelected(item: Movie, position: Int) {
                    val navWithAction = MovieFragmentDirections.actionNavigationMovieToMovieDetailsFragment(item)

                    navController.navigate(navWithAction)
                }

                override fun onErrorRefreshPressed() {
                    viewModel.fetchPopularMovies()
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

    private fun handleObserver(response: NetworkResponse<MovieResponse>, adapter: PreviewAdapter<Movie>?) {
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
        upcomingAdapter = null
        popularAdapter = null
        super.onDestroyView()
    }
}