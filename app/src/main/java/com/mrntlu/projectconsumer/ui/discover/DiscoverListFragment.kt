package com.mrntlu.projectconsumer.ui.discover

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.DiscoverOnBottomSheet
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.main.discover.DiscoverListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverListFragment: BaseFragment<FragmentListBinding>() {

    private val args: DiscoverListFragmentArgs by navArgs()

    @Inject lateinit var viewModelFactory: DiscoverListViewModel.Factory
    private val viewModel: DiscoverListViewModel by viewModels {
        DiscoverListViewModel.provideDiscoverListViewModelFactory(
            viewModelFactory, this, arguments, args.genre,
            args.contentType, sharedViewModel.isNetworkAvailable()
        )
    }

    private var contentAdapter: ContentAdapter<ContentModel>? = null
    private lateinit var contentType: Constants.ContentType
    private var gridCount = 3

    private val discoverOnBottomSheet = object: DiscoverOnBottomSheet {
        override fun onApply(
            genre: String?, status: String?, sort: String, from: Int?, to: Int?,
            animeTheme: String?, gameTBA: Boolean?, gamePlatform: String?
        ) {
            viewModel.startDiscoveryFetch(
                contentType, sort, status, genre, from, to, animeTheme, gameTBA, gamePlatform
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentType = args.contentType

        setMenu()
        setObservers()
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {}

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.sort_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        activity?.let {
                            viewModel.apply {
                                val bottomSheet = DiscoverBottomSheet(
                                    initialSort = sort,
                                    initialGenre = genre,
                                    initialStatus = status,
                                    initialDecade = from?.toString(),
                                    initialAnimeTheme = animeTheme,
                                    initialGameTBA = gameTBA,
                                    initialGamePlatform = gamePlatform,
                                    contentType = contentType,
                                    discoverOnBottomSheet = discoverOnBottomSheet
                                )
                                bottomSheet.show(it.supportFragmentManager, DiscoverBottomSheet.TAG)
                            }
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

        viewModel.discoverResults.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                contentAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                contentAdapter?.setLoadingView()
            } else if (response.isSuccessful() || response.isPaginating) {

                contentAdapter?.setData(
                    response.data!!.toCollection(ArrayList()),
                    response.isPaginationData,
                    response.isPaginationExhausted,
                    response.isPaginating,
                )

                if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)

                    if (viewModel.isRestoringData) {
                        viewModel.isRestoringData = false
                    } else {
                        viewModel.didOrientationChange = false
                    }
                } else if (!response.isPaginating) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.listRV.apply {
            val gridLayoutManager = GridLayoutManager(this.context, gridCount)

            gridLayoutManager.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemViewType = contentAdapter?.getItemViewType(position)
                    return if (
                        itemViewType == RecyclerViewEnum.View.value ||
                        itemViewType == RecyclerViewEnum.Loading.value
                    ) 1 else gridCount
                }
            }

            layoutManager = gridLayoutManager
            contentAdapter = ContentAdapter(
                gridCount = gridCount,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                interaction = object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        val contentType: Constants.ContentType = args.contentType

                        val navWithAction = when(contentType) {
                            Constants.ContentType.ANIME -> TODO()
                            Constants.ContentType.MOVIE -> DiscoverListFragmentDirections.actionDiscoverListFragmentToMovieDetailsFragment(
                                item.id
                            )
                            Constants.ContentType.TV -> DiscoverListFragmentDirections.actionDiscoverListFragmentToTvDetailsFragment(
                                item.id
                            )
                            Constants.ContentType.GAME -> TODO()
                        }

                        navController.navigate(navWithAction)
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.apply {
                            viewModel.startDiscoveryFetch(args.contentType, sort, status, genre, from, to)
                        }
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onExhaustButtonPressed() {
                        viewLifecycleOwner.lifecycleScope.launch {
                            quickScrollToTop()
                        }
                    }
                }
            )
            adapter = contentAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val itemCount = gridLayoutManager.itemCount / gridCount
                    val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition() / gridCount

                    if (isScrolling) {
                        val centerScrollPosition = (gridLayoutManager.findLastCompletelyVisibleItemPosition() + gridLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
                        viewModel.setScrollPosition(centerScrollPosition)
                    }

                    val isScrollingUp = dy <= -90
                    val isScrollingDown = dy >= 10
                    val isThresholdPassed = lastVisibleItemPosition > Constants.PAGINATION_LIMIT.div(gridCount)

                    if (isThresholdPassed && isScrollingUp)
                        binding.topFAB.show()
                    else if (!isThresholdPassed || isScrollingDown)
                        binding.topFAB.hide()

                    contentAdapter?.let {
                        if (
                            isScrolling &&
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(4) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            viewModel.discoverContent()
                        }
                    }
                }
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.discoverResults.removeObservers(this)
            sharedViewModel.windowSize.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }
        contentAdapter = null
        super.onDestroyView()
    }
}