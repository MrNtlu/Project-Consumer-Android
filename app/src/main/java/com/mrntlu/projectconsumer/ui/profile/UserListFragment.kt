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
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.UserListAdapter
import com.mrntlu.projectconsumer.databinding.FragmentUserListBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListInteraction
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
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

    private var popupMenu: PopupMenu? = null
    private var userListAdapter: UserListAdapter? = null

    private var userListDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

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

    override fun onStart() {
        super.onStart()

        if (!viewModel.didOrientationChange && viewModel.userList.value is NetworkResponse.Success) {
            userListAdapter?.setLoadingView()
            viewModel.getUserList()
        }
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
                    context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                        if (userListDeleteLiveData != null && userListDeleteLiveData?.hasActiveObservers() == true)
                            userListDeleteLiveData?.removeObservers(viewLifecycleOwner)

                        val userListModel = when (contentType) {
                            Constants.ContentType.ANIME -> item.animeList[position]
                            Constants.ContentType.MOVIE -> item.movieList[position]
                            Constants.ContentType.TV -> item.tvList[position]
                            Constants.ContentType.GAME -> item.gameList[position]
                        }

                        userListDeleteLiveData = viewModel.deleteUserList(DeleteUserListBody(userListModel.id, contentType.request))

                        userListDeleteLiveData?.observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    userListAdapter?.handleOperation(
                                        Operation(userListModel, -1, OperationEnum.Delete)
                                    )
                                }
                            }
                        }
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
                    val navWithAction = when(contentType) {
                        Constants.ContentType.ANIME -> TODO()
                        Constants.ContentType.MOVIE -> UserListFragmentDirections.actionUserListFragmentToMovieDetailsFragment(
                            item.movieList[position].contentId
                        )
                        Constants.ContentType.TV -> UserListFragmentDirections.actionUserListFragmentToTvDetailsFragment(
                            item.tvList[position].contentId
                        )
                        Constants.ContentType.GAME -> TODO()
                    }
                    navController.navigate(navWithAction)
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


            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    viewModel.setTotalYPosition(dy)
                }
            })
        }
    }

    private fun setObservers() {
        val layoutParams = binding.userListTabLayout.layoutParams as ViewGroup.MarginLayoutParams

        viewModel.totalYScroll.observe(viewLifecycleOwner) { totalYScroll ->
            if (totalYScroll in 0..300) {
                layoutParams.topMargin = -totalYScroll / 2
                binding.userListTabLayout.layoutParams = layoutParams
            } else {
                layoutParams.topMargin = -150
                binding.userListTabLayout.layoutParams = layoutParams
            }
        }

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
                        if (popupMenu == null) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                            popupMenu = PopupMenu(requireContext(), menuItemView)
                            popupMenu!!.menuInflater.inflate(R.menu.sort_dual_menu, popupMenu!!.menu)
                            popupMenu!!.setForceShowIcon(true)
                        }

                        popupMenu?.let {
                            val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                            val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                            for (i in 0..it.menu.size.minus(1)) {
                                val popupMenuItem = it.menu[i]
                                val sortType = Constants.SortUserListRequests[i]

                                popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                    requireContext(),
                                    if(viewModel.sort == sortType.request) selectedColor else unselectedColor
                                )
                                popupMenuItem.title = sortType.name
                            }

                            it.setOnMenuItemClickListener { item ->
                                val newSortType = when(item.itemId) {
                                    R.id.firstSortMenu -> {
                                        setPopupMenuItemVisibility(it, 0)

                                        Constants.SortUserListRequests[0].request
                                    }
                                    R.id.secondSortMenu -> {
                                        setPopupMenuItemVisibility(it, 1)

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

                            it.show()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.userList.removeObservers(this)
            viewModel.totalYScroll.removeObservers(this)
        }
        popupMenu = null
        userListAdapter = null
        super.onDestroyView()
    }
}