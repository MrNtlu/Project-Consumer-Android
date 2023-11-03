package com.mrntlu.projectconsumer.ui.discover

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
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
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
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
            animeTheme: String?, animeDemographics: String?, gameTBA: Boolean?, gamePlatform: String?
        ) {
            binding.listToolbar.title = genre

            viewModel.startDiscoveryFetch(
                contentType, sort, status, genre, from, to,
                animeTheme, animeDemographics, gameTBA, gamePlatform,
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

        setToolbar()
        setObservers()
    }

    private fun setToolbar() {
        binding.listToolbar.apply {
            title = viewModel.genre
            subtitle = getString(R.string.discover_title)
            inflateMenu(R.menu.sort_toolbar_menu)

            var lastTimeClicked: Long = 0

            setOnMenuItemClickListener {
                if (SystemClock.elapsedRealtime() - lastTimeClicked < 550) {
                    return@setOnMenuItemClickListener false
                }
                lastTimeClicked = SystemClock.elapsedRealtime()

                when(it.itemId) {
                    R.id.sortMenu -> {
                        activity?.let {
                            viewModel.apply {
                                val bottomSheet = DiscoverBottomSheet(
                                    initialSort = sort,
                                    initialGenre = genre,
                                    initialStatus = status,
                                    initialDecade = from?.toString(),
                                    initialAnimeTheme = animeTheme,
                                    initialAnimeDemographics = animeDemographics,
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
                true
            }

            setNavigationOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setObservers() {
        if (sharedViewModel.isAltLayout()) {
            gridCount = 1

            setRecyclerView()
        } else {
            sharedViewModel.windowSize.observe(viewLifecycleOwner) {
                val widthSize: WindowSizeClass = it

                gridCount = when(widthSize) {
                    WindowSizeClass.COMPACT -> 2
                    WindowSizeClass.MEDIUM -> 3
                    WindowSizeClass.EXPANDED -> 5
                }

                setRecyclerView()
            }
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
                viewModel.viewModelScope.launch {
                    contentAdapter?.setData(
                        response.data!!.toCollection(ArrayList()),
                        response.isPaginationData,
                        response.isPaginationExhausted,
                        response.isPaginating,
                        viewModel.didOrientationChange,
                    )
                }

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
            val rvLayoutManager = if (sharedViewModel.isAltLayout()) {
                val linearLayoutManager = LinearLayoutManager(this.context)

                val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
                divider.apply {
                    dividerInsetStart = context.dpToPx(119f)
                    dividerInsetEnd = context.dpToPx(8f)
                    dividerThickness = context.dpToPx(1f)
                    isLastItemDecorated = false
                }
                addItemDecoration(divider)

                gridCount = 1
                linearLayoutManager
            } else {
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
                gridLayoutManager
            }

            layoutManager = rvLayoutManager
            contentAdapter = ContentAdapter(
                gridCount = gridCount,
                isRatioDifferent = contentType == Constants.ContentType.GAME,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                isAltLayout = sharedViewModel.isAltLayout(),
                radiusInPx = context.dpToPxFloat(8f),
                sizeMultiplier = if (sharedViewModel.isAltLayout()) 0.8f else 0.9f,
                interaction = object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        if (navController.currentDestination?.id == R.id.discoverListFragment) {
                            val contentType: Constants.ContentType = args.contentType

                            val navWithAction = when(contentType) {
                                Constants.ContentType.ANIME -> DiscoverListFragmentDirections.actionDiscoverListFragmentToAnimeDetailsFragment(
                                    item.id
                                )
                                Constants.ContentType.MOVIE -> DiscoverListFragmentDirections.actionDiscoverListFragmentToMovieDetailsFragment(
                                    item.id
                                )
                                Constants.ContentType.TV -> DiscoverListFragmentDirections.actionDiscoverListFragmentToTvDetailsFragment(
                                    item.id
                                )
                                Constants.ContentType.GAME -> DiscoverListFragmentDirections.actionDiscoverListFragmentToGameDetailsFragment(
                                    item.id
                                )
                            }

                            navController.navigate(navWithAction)
                        }
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
                    val itemCount = rvLayoutManager.itemCount / gridCount
                    val lastVisibleItemPosition = rvLayoutManager.findLastVisibleItemPosition() / gridCount

                    if (isScrolling) {
                        val centerScrollPosition = (rvLayoutManager.findLastCompletelyVisibleItemPosition() + rvLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
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