package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.widget.SearchView
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
import com.mrntlu.projectconsumer.databinding.FragmentMovieSearchBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.main.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentMovieSearchBinding>() {

    private val args: SearchFragmentArgs by navArgs()

    @Inject lateinit var viewModelFactory: SearchViewModel.Factory
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.provideMovieViewModelFactory(viewModelFactory, this, arguments, args.searchQuery, args.searchType, sharedViewModel.isNetworkAvailable())
    }

    private lateinit var searchQuery: String
    private lateinit var searchType: Constants.ContentType

    private var contentAdapter: ContentAdapter<ContentModel>? = null
    private var gridCount = 3

    /* TODO!!!!
    * TODO Check for process death
    *  TODO!!!!!
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchQuery = args.searchQuery
        searchType = args.searchType

        setUI()
        setObservers()
    }

    private fun setUI() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.removeItem(R.id.settingsMenu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_toolbar_menu, menu)

                val searchView = menu.findItem(R.id.searchMenu).actionView as SearchView
                searchView.setQuery(searchQuery, false)
                searchView.isIconified = false
                searchView.clearFocus()

                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query?.isNotEmptyOrBlank() == true) {
                            searchView.clearFocus()

                            searchQuery = query
                            viewModel.startMoviesFetch(searchQuery)
                        }

                        return true
                    }

                    override fun onQueryTextChange(newText: String?) = true
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem) = true
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

        viewModel.searchResults.observe(viewLifecycleOwner) { response ->
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
                        when(args.searchType) {
                            Constants.ContentType.ANIME -> TODO()
                            Constants.ContentType.MOVIE -> {
                                val navWithAction = SearchFragmentDirections.actionSearchFragmentToMovieDetailsFragment(item.id)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.TV -> {
                                val navWithAction = SearchFragmentDirections.actionSearchFragmentToTvDetailsFragment(item.id)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.GAME -> TODO()
                        }
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.startMoviesFetch(searchQuery)
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
        contentAdapter = null
        super.onDestroyView()
    }
}