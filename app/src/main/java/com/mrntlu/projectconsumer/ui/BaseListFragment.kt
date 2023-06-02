package com.mrntlu.projectconsumer.ui

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import kotlinx.coroutines.launch

abstract class BaseListFragment<T: ContentModel>: BaseFragment<FragmentListBinding>() {

    private lateinit var popupMenu: PopupMenu
    protected var contentAdapter: ContentAdapter<T>? = null
    private var sortType: String = Constants.SortRequests[0].request
    protected var gridCount = 3

    protected var isNavigatingBack = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setMenu(
        fetchType: String,
        fetch: (String) -> Unit,
    ) {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.sort_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (contentAdapter?.isLoading == false) {
                            if (!::popupMenu.isInitialized) {
                                val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                                popupMenu = PopupMenu(requireContext(), menuItemView)
                                popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)
                                popupMenu.setForceShowIcon(true)
                            }

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
                                    fetch(sortType)
                                }

                                true
                            }

                            popupMenu.show()
                        }
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

    protected fun setRecyclerView(
        startFetch: (String) -> Unit,
        onItemSelected: (String) -> Unit,
        scrollViewModel: (Int) -> Unit,
        fetch: () -> Unit,
    ) {
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
                interaction = object: Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        isNavigatingBack = true

                        onItemSelected(item.id)
                    }

                    override fun onErrorRefreshPressed() {
                        startFetch(sortType)
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

                    if (isScrolling && !isNavigatingBack) {
                        val centerScrollPosition = (gridLayoutManager.findLastCompletelyVisibleItemPosition() + gridLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
                        scrollViewModel(centerScrollPosition)
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
                            fetch()
                        }
                    }
                }
            })
        }
    }

    protected fun onListObserveHandler(
        response: NetworkListResponse<List<T>>,
        onRestoringAndOrientationChange: () -> Unit
    ) {
        if (response.isFailed()) {
            contentAdapter?.setErrorView(response.errorMessage!!)
        } else if (response.isLoading) {
            contentAdapter?.setLoadingView()
        } else if (response.isSuccessful() || response.isPaginating) {
            val arrayList = response.data!!.toCollection(ArrayList())

            contentAdapter?.setData(
                arrayList,
                response.isPaginationData,
                response.isPaginationExhausted,
                response.isPaginating,
            )

            onRestoringAndOrientationChange()
        }
    }

    protected fun setListeners() {
        binding.topFAB.setOnClickListener {
            binding.listRV.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            sharedViewModel.windowSize.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }
        contentAdapter = null
        super.onDestroyView()
    }
}