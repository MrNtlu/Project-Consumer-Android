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
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListInteraction
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
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

    private var userListAdapter: UserListAdapter? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            when(operation) {
                BottomSheetOperation.INSERT -> {}
                BottomSheetOperation.UPDATE -> {
                    userListAdapter?.handleOperation(
                        Operation(data, -1, OperationEnum.Update)
                    )
                }
                BottomSheetOperation.DELETE -> {
                    userListAdapter?.handleOperation(
                        Operation(data, -1, OperationEnum.Delete)
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

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
                    context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {

                    }
                }

                override fun onUpdatePressed(
                    item: UserList,
                    contentType: Constants.ContentType,
                    position: Int
                ) {
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
                            userListModel.contentId,
                            userListModel.contentExternalId,
                            userListModel.totalSeasons,
                            userListModel.totalEpisodes,
                            onBottomSheetClosedCallback,
                        )
                        userListBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                    }
                }

                override fun onDetailsPressed(
                    item: UserList,
                    contentType: Constants.ContentType,
                    position: Int
                ) {
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
                            userListModel.contentId,
                            userListModel.contentExternalId,
                            userListModel.totalSeasons,
                            userListModel.totalEpisodes,
                            onBottomSheetClosedCallback,
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
                menuInflater.inflate(R.menu.user_list_menu, menu)

                val searchView = menu.findItem(R.id.searchMenu).actionView as SearchView
                searchView.setQuery(searchQuery, false)
                searchView.clearFocus()

                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        context?.hideKeyboard(searchView)

                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        userListAdapter?.search(newText)

                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (!::popupMenu.isInitialized) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                            popupMenu = PopupMenu(requireContext(), menuItemView)
                            popupMenu.menuInflater.inflate(R.menu.sort_dual_menu, popupMenu.menu)
                            popupMenu.setForceShowIcon(true)
                        }

                        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                        for (i in 0..popupMenu.menu.size.minus(1)) {
                            val popupMenuItem = popupMenu.menu[i]
                            val sortType = Constants.SortUserListRequests[i]

                            popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                requireContext(),
                                if(viewModel.sort == sortType.request) selectedColor else unselectedColor
                            )
                            popupMenuItem.title = sortType.name
                        }

                        popupMenu.setOnMenuItemClickListener { item ->
                            val newSortType = when(item.itemId) {
                                R.id.firstSortMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 0)

                                    Constants.SortUserListRequests[0].request
                                }
                                R.id.secondSortMenu -> {
                                    setPopupMenuItemVisibility(popupMenu, 1)

                                    Constants.SortUserListRequests[1].request
                                }
                                else -> { Constants.SortUserListRequests[0].request }
                            }

                            item.isChecked = true

                            if (newSortType != viewModel.sort) {
                                viewModel.setSort(newSortType)
                                viewModel.getUserList()
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