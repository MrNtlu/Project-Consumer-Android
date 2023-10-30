package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ReviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentReviewBinding
import com.mrntlu.projectconsumer.interfaces.ReviewInteraction
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.viewmodels.main.review.ReviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReviewFragment: BaseFragment<FragmentReviewBinding>() {

    private val args: ReviewFragmentArgs by navArgs()

    private lateinit var dialog: LoadingDialog
    private var sortPopupMenu: PopupMenu? = null
    private var isNavigatingBack = false

    private var reviewAdapter: ReviewAdapter? = null

    @Inject
    lateinit var viewModelFactory: ReviewViewModel.Factory
    private val viewModel: ReviewViewModel by viewModels {
        ReviewViewModel.provideReviewViewModelFactory(
            viewModelFactory, this, arguments, args.contentId, args.contentExternalId,
            if (args.contentExternalIntId > 0) args.contentExternalIntId else null, args.contentType
        )
    }

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
        setRecyclerView()
        setObservers()
        setListeners()

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
                                    if(viewModel.sort == sortType.request) selectedColor else unselectedColor
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

                                if (newSortType != viewModel.sort) {
                                    viewModel.setSort(newSortType)
                                    viewModel.getReviews(shouldRefresh = true)
                                }

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

    private fun setObservers() {
        viewModel.reviewList.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                reviewAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                reviewAdapter?.setLoadingView()
            } else if (response.isSuccessful() || response.isPaginating) {
                val arrayList = response.data!!.toCollection(ArrayList())

                reviewAdapter?.setData(
                    arrayList,
                    response.isPaginationData,
                    response.isPaginationExhausted,
                    response.isPaginating
                )

                if (viewModel.didOrientationChange) {
                    binding.reviewsRV.scrollToPosition(viewModel.scrollPosition - 1)

                    viewModel.didOrientationChange = false
                } else if (!response.isPaginating) {
                    binding.reviewsRV.scrollToPosition(viewModel.scrollPosition - 1)
                    isNavigatingBack = false
                }
            }
        }
    }

    private fun setListeners() {
        binding.writeReviewFAB.setOnClickListener {
            //TODO Click
        }
    }

    private fun setRecyclerView() {
        binding.reviewsRV.apply {
            val linearLayoutManager = LinearLayoutManager(this.context)

            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
            divider.apply {
//                dividerInsetStart = context.dpToPx(8f)
//                dividerInsetEnd = context.dpToPx(8f)
                dividerThickness = context.dpToPx(1f)
                isLastItemDecorated = false
            }
            addItemDecoration(divider)

            layoutManager = linearLayoutManager

            reviewAdapter = ReviewAdapter(!sharedViewModel.isLightTheme(), object: ReviewInteraction {
                override fun onEditClicked(item: Review, position: Int) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(item: Review, position: Int) {
                    TODO("Not yet implemented")
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getReviews(shouldRefresh = true)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {
                    viewLifecycleOwner.lifecycleScope.launch {
                        quickScrollToTop()
                    }
                }

            })
            adapter = reviewAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val itemCount = linearLayoutManager.itemCount
                    val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()

                    if (isScrolling && !isNavigatingBack) {
                        val centerScrollPosition = (linearLayoutManager.findLastCompletelyVisibleItemPosition() + linearLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
                        viewModel.setScrollPosition(centerScrollPosition)
                    }

                    val isScrollingUp = dy <= -90
                    val isScrollingDown = dy >= 10

                    if (isScrollingUp)
                        binding.writeReviewFAB.extend()
                    else if (isScrollingDown)
                        binding.writeReviewFAB.shrink()

                    reviewAdapter?.let {
                        if (
                            isScrolling &&
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(4) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            viewModel.getReviews()
                        }
                    }
                }
            })
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
        viewModel.reviewList.removeObservers(viewLifecycleOwner)

        reviewAdapter = null
        sortPopupMenu = null
        super.onDestroyView()
    }
}