package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.UserListAdapter
import com.mrntlu.projectconsumer.databinding.FragmentUserListBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.UserListInteraction
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.viewmodels.main.profile.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListFragment: BaseFragment<FragmentUserListBinding>() {

    private companion object {
        private const val KEY_VALUE = "content_type"
    }

    private val viewModel: UserListViewModel by viewModels()

    private var searchQuery: String? = null
    private var contentType: Constants.ContentType = Constants.ContentType.MOVIE

    private lateinit var dialog: LoadingDialog
    private lateinit var popupMenu: PopupMenu
    private lateinit var sortPopupMenu: PopupMenu

    private var userListAdapter: UserListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    //TODO Menu set status quick menu, open dialog let them select and save.
    // This can be expanded into status + score and implement it to consume later too.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        savedInstanceState?.let {
            contentType = Constants.ContentType.fromStringValue(
                it.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setUI()
        setMenu()
        setListeners()
        setRecyclerView()
        setObservers()
    }

    private fun setUI() {
        binding.apply {
            userListTabLayout.apply {
                if (userListTabLayout.tabCount < Constants.TabList.size) {
                    for (tab in Constants.TabList) {
                        addTab(
                            userListTabLayout.newTab().setText(tab),
                            tab == contentType.value
                        )
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            userListTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position) {
                        0 -> {
                            contentType = Constants.ContentType.MOVIE
                        }
                        1 -> {
                            contentType = Constants.ContentType.TV
                        }
                        2 -> {
                            contentType = Constants.ContentType.ANIME
                        }
                        3 -> {
                            contentType = Constants.ContentType.GAME
                        }
                        else -> {}
                    }

                    userListAdapter?.changeContentType(contentType)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setRecyclerView() {
        binding.userListRV.apply {
            val linearLayout = LinearLayoutManager(context)
            layoutManager = linearLayout

            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))

            userListAdapter = UserListAdapter(object: UserListInteraction {
                override fun onDeletePressed(
                    item: UserList,
                    contentType: Constants.ContentType,
                    position: Int
                ) {
                    //TODO Show yes no dialog, if yes show loading dialog and call handle operation
                }

                override fun onUpdatePressed(
                    item: UserList,
                    contentType: Constants.ContentType,
                    position: Int
                ) {
                    //TODO Open bottom sheet dialog, season and episode should be both text and + button next to it.
                    // On save loading dialog and handle operation
                    // Loading and Failure should be similar to DetailsBottomSheet, no need to open dialog.
                    // Consider making it full screen bottom sheet if necessary.
                    val userListModel = when (contentType) {
                        Constants.ContentType.ANIME -> item.animeList[position]
                        Constants.ContentType.MOVIE -> item.movieList[position]
                        Constants.ContentType.TV -> item.tvList[position]
                        Constants.ContentType.GAME -> item.gameList[position]
                    }

                    activity?.let {
                        val userListBottomSheet = UserListBottomSheet(
                            userListModel,
                            contentType,
                            BottomSheetState.EDIT,
                            userListModel.totalSeasons,
                            userListModel.totalEpisodes,
                        )
                        userListBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                    }
                }

                override fun onDetailsPressed(
                    item: UserList,
                    contentType: Constants.ContentType,
                    position: Int
                ) {
                    //TODO Like details, show UI. There should be edit button and change UI on bottom sheet.
                    val userListModel = when (contentType) {
                        Constants.ContentType.ANIME -> item.animeList[position]
                        Constants.ContentType.MOVIE -> item.movieList[position]
                        Constants.ContentType.TV -> item.tvList[position]
                        Constants.ContentType.GAME -> item.gameList[position]
                    }

                    activity?.let {
                        val userListBottomSheet = UserListBottomSheet(
                            userListModel,
                            contentType,
                            BottomSheetState.VIEW,
                            userListModel.totalSeasons,
                            userListModel.totalEpisodes,
                        )
                        userListBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                    }
                }

                override fun onItemSelected(item: UserList, position: Int) {
                    //TODO Navigate, change interaction we need content type too.
                    // OnStart (On Back Pressed), refresh the data.
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getUserList()
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {}
            })
            adapter = userListAdapter
        }
    }

    private fun setObservers() {
        viewModel.userList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> userListAdapter?.setErrorView(response.errorMessage)
                NetworkResponse.Loading -> userListAdapter?.setLoadingView()
                is NetworkResponse.Success -> {
                    userListAdapter?.setData(response.data.data)
                }
            }
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.removeItem(R.id.settingsMenu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.consume_later_menu, menu)

                val searchView = menu.findItem(R.id.searchMenu).actionView as SearchView
                searchView.setQuery(searchQuery, false)
                searchView.clearFocus()

                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        context?.hideKeyboard(searchView)

                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
//                        consumeLaterAdapter?.search(newText)

                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (!::sortPopupMenu.isInitialized) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                            sortPopupMenu = PopupMenu(requireContext(), menuItemView)
                            sortPopupMenu.menuInflater.inflate(R.menu.sort_extra_menu, sortPopupMenu.menu)
                            sortPopupMenu.setForceShowIcon(true)
                        }

                        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                        for (i in 0..sortPopupMenu.menu.size.minus(1)) {
                            val popupMenuItem = sortPopupMenu.menu[i]
                            val sortType = Constants.SortConsumeLaterRequests[i]

//                            popupMenuItem.iconTintList = ContextCompat.getColorStateList(
//                                requireContext(),
//                                if(viewModel.sort == sortType.request) selectedColor else unselectedColor
//                            )
                            popupMenuItem.title = sortType.name
                        }

                        sortPopupMenu.setOnMenuItemClickListener { item ->
                            val newSortType = when(item.itemId) {
                                R.id.firstSortMenu -> {
                                    setPopupMenuItemVisibility(sortPopupMenu, 0)

                                    Constants.SortConsumeLaterRequests[0].request
                                }
                                R.id.secondSortMenu -> {
                                    setPopupMenuItemVisibility(sortPopupMenu, 1)

                                    Constants.SortConsumeLaterRequests[1].request
                                }
                                R.id.thirdSortMenu -> {
                                    setPopupMenuItemVisibility(sortPopupMenu, 2)

                                    Constants.SortConsumeLaterRequests[2].request
                                }
                                R.id.forthSortMenu -> {
                                    setPopupMenuItemVisibility(sortPopupMenu, 3)

                                    Constants.SortConsumeLaterRequests[3].request
                                }
                                else -> { Constants.SortConsumeLaterRequests[0].request }
                            }

                            item.isChecked = true

//                            if (newSortType != viewModel.sort) {
//                                viewModel.setSort(newSortType)
//                                viewModel.getConsumeLater()
//                            }

                            true
                        }

                        sortPopupMenu.show()
                    }
                    R.id.filterMenu -> {
                        if (!::popupMenu.isInitialized) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.filterMenu)
                            popupMenu = PopupMenu(requireContext(), menuItemView)
                            popupMenu.menuInflater.inflate(R.menu.filter_consume_later_menu, popupMenu.menu)
                            popupMenu.setForceShowIcon(true)
                        }

                        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                        for (i in 0..popupMenu.menu.size.minus(1)) {
                            val popupMenuItem = popupMenu.menu[i]
                            val contentType = Constants.ContentType.values()[i]

//                            popupMenuItem.iconTintList = ContextCompat.getColorStateList(
//                                requireContext(),
//                                if(viewModel.filter == contentType.request) selectedColor else unselectedColor
//                            )
                            popupMenuItem.title = contentType.value
                        }

                        popupMenu.setOnMenuItemClickListener { item ->
                            val newFilterType = when (item.itemId) {
                                R.id.firstFilterMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 0)

                                    Constants.ContentType.values()[0]
                                }
                                R.id.secondFilterMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 1)

                                    Constants.ContentType.values()[1]
                                }
                                R.id.thirdFilterMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 2)

                                    Constants.ContentType.values()[2]
                                }
                                R.id.forthFilterMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 3)

                                    Constants.ContentType.values()[3]
                                }
                                else -> { Constants.ContentType.values()[0] }
                            }

                            item.isChecked = true

//                            viewModel.setFilter(
//                                if (newFilterType.request != viewModel.filter)
//                                    newFilterType.request
//                                else null
//                            )
//                            viewModel.getConsumeLater()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.userList.removeObservers(this)
        }
        userListAdapter = null
        super.onDestroyView()
    }
}