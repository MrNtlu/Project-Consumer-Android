package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BaseListFragment
import com.mrntlu.projectconsumer.viewmodels.main.movie.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MovieListFragment: BaseListFragment<Movie>() {

    private val args: MovieListFragmentArgs by navArgs()

    @Inject lateinit var viewModelFactory: MovieViewModel.Factory
    private val viewModel: MovieViewModel by viewModels {
        MovieViewModel.provideMovieViewModelFactory(viewModelFactory, this, arguments, args.fetchType, sharedViewModel.isNetworkAvailable())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setListeners()
    }

    private fun setObservers() {
        sharedViewModel.windowSize.observe(viewLifecycleOwner) {
            val widthSize: WindowSizeClass = it

            gridCount = when(widthSize) {
                WindowSizeClass.COMPACT -> 2
                WindowSizeClass.MEDIUM -> 3
                WindowSizeClass.EXPANDED -> 5
            }

            setRecyclerView(
                startFetch = { viewModel.startMoviesFetch() },
                onItemSelected = { itemId ->
                    val navWithAction = MovieListFragmentDirections.actionMovieListFragmentToMovieDetailsFragment(itemId)

                    navController.navigate(navWithAction)
                },
                scrollViewModel = { position -> viewModel.setScrollPosition(position) },
                fetch = { viewModel.fetchMovies() }
            )
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailable = it
        }

        viewModel.movies.observe(viewLifecycleOwner) { response ->
            onListObserveHandler(response) {
                if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)

                    if (viewModel.isRestoringData) {
                        viewModel.isRestoringData = false
                    } else {
                        viewModel.didOrientationChange = false
                    }
                } else if (!response.isPaginating) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)
                    isNavigatingBack = false
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewModel.movies.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}