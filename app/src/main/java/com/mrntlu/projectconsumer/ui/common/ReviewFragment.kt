package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentReviewBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.ui.discover.DiscoverBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewFragment: BaseFragment<FragmentReviewBinding>() {

    private lateinit var dialog: LoadingDialog
    private var sortPopupMenu: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setToolbar()

//        binding.writeReviewFAB.shrink()
    }

    private fun setToolbar() {
        binding.reviewsToolbar.apply {
            setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        //TODO add if not loading

                        if (sortPopupMenu == null) {
                            val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                            sortPopupMenu = PopupMenu(requireContext(), menuItemView)
                            sortPopupMenu!!.menuInflater.inflate(R.menu.sort_menu, sortPopupMenu!!.menu)
                            sortPopupMenu!!.setForceShowIcon(true)
                        }

                        sortPopupMenu?.let {
                            val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                            val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                            for (i in 0..it.menu.size.minus(1)) {
                                val popupMenuItem = it.menu[i]
                                val sortType = Constants.SortReviewRequests[i]

                                popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                    requireContext(),
                                    unselectedColor
//                                    if(viewModel.sort == sortType.request) selectedColor else unselectedColor
                                )
                                popupMenuItem.title = sortType.name
                            }

                            it.setOnMenuItemClickListener { item ->
                                val newSortType = when(item.itemId) {
                                    R.id.firstSortMenu -> {
                                        setPopupMenuItemVisibility(it, 0)

                                        Constants.SortReviewRequests[0].request
                                    }
                                    R.id.secondSortMenu -> {
                                        setPopupMenuItemVisibility(it, 1)

                                        Constants.SortReviewRequests[1].request
                                    }
                                    R.id.thirdSortMenu -> {
                                        setPopupMenuItemVisibility(it, 2)

                                        Constants.SortReviewRequests[2].request
                                    }
                                    else -> { Constants.SortReviewRequests[0].request }
                                }

                                item.isChecked = true

//                                if (newSortType != viewModel.sort) {
//                                    resetSearchView()
//                                    viewModel.setSort(newSortType)
//                                    viewModel.getConsumeLater()
//                                }

                                true
                            }

                            it.show()
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

    private fun setPopupMenuItemVisibility(popupMenu: PopupMenu, selectedIndex: Int) {
        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

        for(i in 0..popupMenu.menu.size.minus(1)) {
            popupMenu.menu[i].iconTintList = ContextCompat.getColorStateList(requireContext(), if(i == selectedIndex) selectedColor else unselectedColor)
        }
    }

    override fun onDestroyView() {

        sortPopupMenu = null
        super.onDestroyView()
    }
}