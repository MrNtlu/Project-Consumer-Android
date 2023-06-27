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
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentUserListBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.viewmodels.main.profile.UserListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListFragment: BaseFragment<FragmentUserListBinding>() {

    private val viewModel: UserListViewModel by viewModels()

    private var searchQuery: String? = null
    private lateinit var dialog: LoadingDialog
    private lateinit var popupMenu: PopupMenu
    private lateinit var sortPopupMenu: PopupMenu

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

        setObservers()
        setMenu()
    }

    private fun setUI() {
        binding.apply {

        }
    }

    private fun setRecyclerView() {
        binding.userListRV.apply {

        }
    }

    private fun setObservers() {
        viewModel.userList.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> TODO()
                NetworkResponse.Loading -> TODO()
                is NetworkResponse.Success -> {

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
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.userList.removeObservers(this)
        }
        super.onDestroyView()
    }
}