package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
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
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.movie.MovieViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MovieListFragment: BaseFragment<FragmentMovieListBinding>() {

    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()
    private val args: MovieListFragmentArgs by navArgs()

    @Inject lateinit var viewModelFactory: MovieViewModel.Factory
    private val viewModel: MovieViewModel by viewModels {
        MovieViewModel.provideMovieViewModelFactory(viewModelFactory, this, arguments, args.fetchType, sharedViewModel.isNetworkAvailable())
    }

    private lateinit var popupMenu: PopupMenu
    private var movieAdapter: MovieAdapter? = null
    private var sortType: String = Constants.SortRequests[0].request
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

        //TODO Test pagination stuck case where when you navigate and comeback you only see pagination. Test with 5 sec sleep etc.
        setMenu()
        setObservers()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.removeItem(R.id.settingsMenu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.sort_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (!::popupMenu.isInitialized) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                            popupMenu = PopupMenu(requireContext(), menuItemView)
                            popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)
                            popupMenu.setForceShowIcon(true)
                        }
                        val fetchType = args.fetchType

                        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                        for (i in 0..popupMenu.menu.size.minus(1)) {
                            val popupMenuItem = popupMenu.menu[i]
                            val sortRequest = when(fetchType) {
                                FetchType.UPCOMING.tag -> Constants.SortUpcomingRequests[i]
                                else -> Constants.SortRequests[i]
                            }

                            popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                requireContext(),
                                if(sortType == sortRequest.request) selectedColor else unselectedColor
                            )
                            popupMenuItem.title = sortRequest.name
                        }

                        popupMenu.setOnMenuItemClickListener { item ->
                            val newSortType = when (item.itemId) {
                                R.id.firstSortMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 0)

                                    when(fetchType) {
                                        FetchType.UPCOMING.tag -> Constants.SortUpcomingRequests[0].request
                                        else -> Constants.SortRequests[0].request
                                    }
                                }
                                R.id.secondSortMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 1)

                                    when(fetchType) {
                                        FetchType.UPCOMING.tag -> Constants.SortUpcomingRequests[1].request
                                        else -> Constants.SortRequests[1].request
                                    }
                                }
                                R.id.thirdSortMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 2)

                                    when(fetchType) {
                                        FetchType.UPCOMING.tag -> Constants.SortUpcomingRequests[2].request
                                        else -> Constants.SortRequests[2].request
                                    }
                                }
                                else -> { Constants.SortRequests[0].request }
                            }

                            item.isChecked = true

                            if (newSortType != sortType) {
                                sortType = newSortType
                                viewModel.startMoviesFetch(sortType)
                            }

                            true
                        }

                        popupMenu.show()
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setPopupMenuItemVisibility(popupMenu: PopupMenu, selectedIndex: Int) {
        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

        for(i in 0..popupMenu.menu.size.minus(1)) {
            popupMenu.menu[i].iconTintList = ContextCompat.getColorStateList(requireContext(), if(i == selectedIndex) selectedColor else unselectedColor)
        }
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
                        val navWithAction = MovieListFragmentDirections.actionMovieListFragmentToMovieDetailsFragment(item)

                        navController.navigate(navWithAction)
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.startMoviesFetch(sortType)
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

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailable = it
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
            sharedViewModel.networkStatus.removeObservers(this)
        }
        movieAdapter = null
        super.onDestroyView()
    }
}