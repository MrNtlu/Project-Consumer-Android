package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.MovieAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieListBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.movie.MovieViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MovieListFragment: BaseFragment<FragmentMovieListBinding>() {

    private val viewModel: MovieViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()
    private val args: MovieListFragmentArgs by navArgs()

    private var movieAdapter: MovieAdapter? = null
    private var sortType: String = Constants.SortUpcomingRequests[0]
    private var gridCount = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setTag(args.fetchType)
        if (!viewModel.isRestoringData) {
            when(args.fetchType) {
                FetchType.UPCOMING.tag -> viewModel.fetchUpcomingMovies(sortType)
                else -> viewModel.fetchPopularMovies(sortType)
            }
        }

        setMenu()
        setObservers()
    }

    //TODO Set sort/filter option
    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.clear()
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean { return false }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setRecyclerView() {
        binding.upcomingRV.apply {
            val gridLayoutManager = GridLayoutManager(this.context, gridCount)

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
                interaction = object: Interaction<Movie> {
                    override fun onItemSelected(item: Movie, position: Int) {
                        printLog("Item Selected $item")
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.refreshData()
                    }

                    override fun onExhaustButtonPressed() {
                        viewLifecycleOwner.lifecycleScope.launch {
                            quickScrollToTop()
                        }
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
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(2) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            printLog("Paginating ${it.isPaginating} ${it.canPaginate} ${it.isPaginating}")

                            viewModel.fetchMovies()
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

        viewModel.movies.observe(viewLifecycleOwner) { response ->
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
            viewModel.movies.removeObservers(this)
            sharedViewModel.windowSize.removeObservers(this)
        }
        movieAdapter = null
        super.onDestroyView()
    }
}