package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentSearchBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.main.discover.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: BaseFragment<FragmentSearchBinding>() {

    private val args: SearchFragmentArgs by navArgs()

    @Inject lateinit var viewModelFactory: SearchViewModel.Factory
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.provideSearchViewModelFactory(viewModelFactory, this, arguments, args.searchQuery, args.searchType, sharedViewModel.isNetworkAvailable())
    }

    private var contentAdapter: ContentAdapter<ContentModel>? = null
    private var searchView: SearchView? = null
    private var searchMenu: MenuItem? = null
    private var popupMenu: PopupMenu? = null
    private var gridCount = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setObservers()
    }

    private fun setToolbar() {
        binding.searchToolbar.apply {
            title = viewModel.contentType.value
            subtitle = "🔍 ${viewModel.search}"
            setNavigationOnClickListener { navController.popBackStack() }

            inflateMenu(R.menu.search_toolbar_menu)

            searchMenu = menu.findItem(R.id.searchMenu)
            searchView = searchMenu?.actionView as SearchView

            try {
                val backgroundView = searchView?.findViewById(androidx.appcompat.R.id.search_plate) as? View
                backgroundView?.background = null
            } catch (_: Exception){}

            searchView?.queryHint = getString(R.string.search)
            searchView?.setQuery(viewModel.search, false)
            searchView?.clearFocus()

            searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query?.isNotEmptyOrBlank() == true) {
                        searchView?.clearFocus()
                        hideKeyboard()
                        viewModel.startContentFetch(query)
                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })

            setOnMenuItemClickListener { menuItem ->
                hideKeyboard()

                when(menuItem.itemId) {
                    R.id.filterMenu -> {
                        if (contentAdapter?.isLoading == false) {
                            if (popupMenu == null) {
                                val menuItemView = requireActivity().findViewById<View>(R.id.filterMenu)
                                popupMenu = PopupMenu(requireContext(), menuItemView)
                                popupMenu!!.menuInflater.inflate(R.menu.filter_consume_later_menu, popupMenu!!.menu)
                                popupMenu!!.setForceShowIcon(true)
                            }

                            popupMenu?.let {
                                val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                                val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                                for (i in 0..it.menu.size.minus(1)) {
                                    val popupMenuItem = it.menu[i]
                                    val contentType = Constants.ContentType.values()[i]

                                    popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                        requireContext(),
                                        if(viewModel.contentType.request == contentType.request) selectedColor else unselectedColor
                                    )
                                    popupMenuItem.title = contentType.value
                                }

                                it.setOnMenuItemClickListener { item ->
                                    val newFilterType = when (item.itemId) {
                                        R.id.firstFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 0)

                                            Constants.ContentType.values()[0]
                                        }
                                        R.id.secondFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 1)

                                            Constants.ContentType.values()[1]
                                        }
                                        R.id.thirdFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 2)

                                            Constants.ContentType.values()[2]
                                        }
                                        R.id.forthFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 3)

                                            Constants.ContentType.values()[3]
                                        }
                                        else -> { Constants.ContentType.values()[0] }
                                    }

                                    item.isChecked = true

                                    if (newFilterType.request != viewModel.contentType.request) {
                                        viewModel.setContentTypeValue(newFilterType)
                                        viewModel.startContentFetch(viewModel.search, true)
                                    }

                                    true
                                }

                                it.show()
                            }
                        }
                    }
                }
                true
            }
        }
    }

    private fun setPopupMenuItemVisibility(popupMenu: PopupMenu, selectedIndex: Int) {
        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

        for(i in 0..popupMenu.menu.size.minus(1)) {
            popupMenu.menu[i].iconTintList = ContextCompat.getColorStateList(requireContext(), if(i == selectedIndex) selectedColor else unselectedColor)
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

        viewModel.searchResults.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                contentAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                contentAdapter?.setLoadingView()
            } else if (response.isSuccessful() || response.isPaginating) {
                binding.searchToolbar.apply {
                    title = viewModel.contentType.value
                    subtitle = "🔍 ${viewModel.search}"
                }

                contentAdapter?.setData(
                    response.data!!.toCollection(ArrayList()),
                    response.isPaginationData,
                    response.isPaginationExhausted,
                    response.isPaginating,
                    viewModel.didOrientationChange,
                )

                if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                    binding.movieSearchRV.scrollToPosition(viewModel.scrollPosition - 1)

                    if (viewModel.isRestoringData) {
                        viewModel.isRestoringData = false
                    } else {
                        viewModel.didOrientationChange = false
                    }
                } else if (!response.isPaginating) {
                    binding.movieSearchRV.scrollToPosition(viewModel.scrollPosition - 1)
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.movieSearchRV.apply {
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
                isRatioDifferent = viewModel.contentType == Constants.ContentType.GAME,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                isAltLayout = sharedViewModel.isAltLayout(),
                radiusInPx = context.dpToPxFloat(8f),
                sizeMultiplier = if (sharedViewModel.isAltLayout()) 0.8f else 0.9f,
                interaction = object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        if (navController.currentDestination?.id == R.id.searchFragment) {
                            when(viewModel.contentType) {
                                Constants.ContentType.ANIME -> {
                                    val navWithAction = SearchFragmentDirections.actionSearchFragmentToAnimeDetailsFragment(item.id)
                                    navController.navigate(navWithAction)
                                }
                                Constants.ContentType.MOVIE -> {
                                    val navWithAction = SearchFragmentDirections.actionSearchFragmentToMovieDetailsFragment(item.id)
                                    navController.navigate(navWithAction)
                                }
                                Constants.ContentType.TV -> {
                                    val navWithAction = SearchFragmentDirections.actionSearchFragmentToTvDetailsFragment(item.id)
                                    navController.navigate(navWithAction)
                                }
                                Constants.ContentType.GAME -> {
                                    val navWithAction = SearchFragmentDirections.actionSearchFragmentToGameDetailsFragment(item.id)
                                    navController.navigate(navWithAction)
                                }
                            }
                        }
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.startContentFetch(viewModel.search)
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

                    contentAdapter?.let {
                        if (
                            isScrolling &&
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(4) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            viewModel.searchContentByTitle()
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
            viewModel.searchResults.removeObservers(this)
            sharedViewModel.windowSize.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }

        searchView = null
        searchMenu = null
        popupMenu = null
        contentAdapter = null
        super.onDestroyView()
    }
}