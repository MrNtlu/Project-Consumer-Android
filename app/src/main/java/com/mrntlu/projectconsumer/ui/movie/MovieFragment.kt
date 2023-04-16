package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.MovieAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.*
import com.mrntlu.projectconsumer.viewmodels.movie.MovieViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment : BaseFragment<FragmentMovieBinding>() {

    private val viewModel: MovieViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    private var movieAdapter: MovieAdapter? = null
    private var gridCount = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setTag(FetchType.UPCOMING)
        setObservers()
    }

    private fun setRecyclerView() {
        binding.upcomingRV.apply {
            val gridLayoutManager = GridLayoutManager(this.context, gridCount)

            //TODO Make pagination loading Jetpack Compsose
            // Pass the gridCount and add shimmer layout gridLayoutTimes.
            gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemViewType = movieAdapter?.getItemViewType(position)
                    return if (
                        itemViewType == RecyclerViewEnum.View.value ||
                        itemViewType == RecyclerViewEnum.Loading.value
                    ) 1 else gridCount
                }
            }

            layoutManager = gridLayoutManager
            movieAdapter = MovieAdapter(
                gridCount = gridCount,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                interaction =  object: Interaction<Movie> {
                    override fun onItemSelected(item: Movie, position: Int) {
                        printLog("Item Selected $item")
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.refreshData()
                    }
                }
            )
            adapter = movieAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val itemCount = gridLayoutManager.itemCount / gridCount
                    val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition()

                    val centerScrollPosition = (gridLayoutManager.findLastCompletelyVisibleItemPosition() + gridLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
                    viewModel.setScrollPosition(centerScrollPosition)

                    movieAdapter?.let {
                        if (
                            isScrolling &&
                            lastVisibleItemPosition >= itemCount.minus(2) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            viewModel.fetchUpcomingMovies(Constants.SortUpcomingRequests[0])
                        }
                    }
                }
            })
        }
    }

    private fun setObservers() {
        sharedViewModel.windowSize.observe(viewLifecycleOwner) {
            val widthSize: WindowSizeClass = it

            gridCount = when(widthSize) {
                WindowSizeClass.COMPACT -> 2
                WindowSizeClass.MEDIUM -> 3
                WindowSizeClass.EXPANDED -> 5
            }

            setRecyclerView()
        }

        viewModel.upcomingMovies.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkListResponse.Failure -> {
                    movieAdapter?.setErrorView(response.errorMessage)
                }
                is NetworkListResponse.Loading -> {
                    movieAdapter?.setLoadingView(response.isPaginating)
                }
                is NetworkListResponse.Success -> {
                    movieAdapter?.setData(response.data.toCollection(ArrayList()), response.isPaginationData, response.isPaginationExhausted)

                    if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                        binding.upcomingRV.scrollToPosition(viewModel.scrollPosition - 1)

                        if (viewModel.isRestoringData) {
                            viewModel.isRestoringData = false
                        } else {
                            viewModel.didOrientationChange = false
                        }
                    } else {
                        binding.upcomingRV.scrollToPosition(viewModel.scrollPosition - 1)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.upcomingMovies.removeObservers(this)
            sharedViewModel.windowSize.removeObservers(this)
        }
        movieAdapter = null
        super.onDestroyView()
    }
}