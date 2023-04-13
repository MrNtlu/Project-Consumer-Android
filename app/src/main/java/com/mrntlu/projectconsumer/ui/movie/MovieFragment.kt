package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@AndroidEntryPoint
class MovieFragment : BaseFragment<FragmentMovieBinding>() {

    private val viewModel: MovieViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    private var movieAdapter: MovieAdapter? = null
    private var gridCount = 3
    private var heightSpan: Double = 4.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setRecyclerView() {
        binding.upcomingRV.apply {
            val gridLayoutManager = object: GridLayoutManager(this.context, gridCount) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
                    lp?.height = (height / heightSpan).toInt()
                    lp?.width = (width / gridCount) - (gridCount * 8)

                    return true
                }
            }
            layoutManager = gridLayoutManager
            movieAdapter = MovieAdapter(object: Interaction<Movie> {
                override fun onItemSelected(item: Movie, position: Int) {
                    printLog("Item Selected $item")
                }

                override fun onErrorRefreshPressed() {

                }

                override fun onExhaustButtonPressed() {
                    TODO("Not yet implemented")
                }

            })
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
//                    viewModel.setScrollPosition(centerScrollPosition)

                    movieAdapter?.let {
                        if (
                            isScrolling &&
                            lastVisibleItemPosition >= itemCount.minus(5) &&
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
            val widthSize = it.first
            val heightSize = it.second

            when(widthSize) {
                WindowSizeClass.COMPACT -> {
                    gridCount = 2

                    heightSpan = when(heightSize) {
                        WindowSizeClass.COMPACT -> {
                            2.0
                        }
                        WindowSizeClass.MEDIUM -> {
                            compactMediumCalculator(it.third)
                        }
                        WindowSizeClass.EXPANDED -> {
                            1.0
                        }
                    }
                }
                WindowSizeClass.MEDIUM -> {
                    gridCount = 3

                    heightSpan = when(heightSize) {
                        WindowSizeClass.COMPACT -> {
                            2.0
                        }
                        WindowSizeClass.MEDIUM -> {
                            2.2
                        }
                        WindowSizeClass.EXPANDED -> {
                            3.0
                        }
                    }
                }
                WindowSizeClass.EXPANDED -> {
                    gridCount = 5

                    heightSpan = when(heightSize) {
                        WindowSizeClass.COMPACT -> {
                            1.3
                        }
                        WindowSizeClass.MEDIUM -> {
                            expandedMediumCalculator(it.third)
                        }
                        WindowSizeClass.EXPANDED -> {
                            4.0
                        }
                    }
                }
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
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        movieAdapter = null
        super.onDestroyView()
    }
}