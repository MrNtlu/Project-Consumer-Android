package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ReviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentReviewBinding
import com.mrntlu.projectconsumer.interfaces.ReviewInteraction
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.review.ReviewViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReviewFragment: BaseFragment<FragmentReviewBinding>() {

    private val args: ReviewFragmentArgs by navArgs()

    private var orientationEventListener: OrientationEventListener? = null

    private var confirmDialog: AlertDialog? = null
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

        orientationEventListener = object : OrientationEventListener(view.context) {
            override fun onOrientationChanged(orientation: Int) {
                val defaultPortrait = 0
                val upsideDownPortrait = 180
                val rightLandscape = 90
                val leftLandscape = 270

                viewModel.viewModelScope.launch {
                    when {
                        isWithinOrientationRange(orientation, defaultPortrait) -> {
                            viewModel.setNewOrientation(Orientation.Portrait)
                        }
                        isWithinOrientationRange(orientation, leftLandscape) -> {
                            viewModel.setNewOrientation(Orientation.Landscape)
                        }
                        isWithinOrientationRange(orientation, upsideDownPortrait) -> {
                            viewModel.setNewOrientation(Orientation.PortraitReverse)
                        }
                        isWithinOrientationRange(orientation, rightLandscape) -> {
                            viewModel.setNewOrientation(Orientation.LandscapeReverse)
                        }
                    }
                }
            }
        }
        orientationEventListener?.enable()

        setToolbar()
        setObservers()
        setListeners()
        setRecyclerView()
    }

    private suspend fun isWithinOrientationRange(
        currentOrientation: Int, targetOrientation: Int, epsilon: Int = 30
    ): Boolean = withContext(Dispatchers.Default){
        currentOrientation > targetOrientation - epsilon && currentOrientation < targetOrientation + epsilon
    }

    override fun onStart() {
        super.onStart()

        if (!viewModel.didOrientationChange && viewModel.reviewList.value != null) {
            reviewAdapter?.setLoadingView()
            viewModel.getReviews(shouldRefresh = true)
        }
    }

    private fun setToolbar() {
        binding.reviewsToolbar.apply {
            subtitle = args.contentTitle

            setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (viewModel.reviewList.value?.isSuccessful() == true) {
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

                viewModel.viewModelScope.launch {
                    reviewAdapter?.setData(
                        arrayList,
                        response.isPaginationData,
                        response.isPaginationExhausted,
                        response.isPaginating,
                        viewModel.didOrientationChange
                    )
                }

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
            if (navController.currentDestination?.id == R.id.reviewFragment) {
                val navWithAction = ReviewFragmentDirections.actionReviewFragmentToReviewCreateFragment(
                    review = null,
                    contentId = args.contentId,
                    contentExternalId = args.contentExternalId,
                    contentExternalIntId = args.contentExternalIntId,
                    contentType = args.contentType,
                    contentTitle = args.contentTitle,
                )

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setRecyclerView() {
        binding.reviewsRV.apply {
            val linearLayoutManager = LinearLayoutManager(this.context)

            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
            divider.apply {
                dividerThickness = context.dpToPx(1f)
                isLastItemDecorated = false
            }
            addItemDecoration(divider)

            layoutManager = linearLayoutManager

            reviewAdapter = ReviewAdapter(
                sharedViewModel.isLoggedIn(),
                object: ReviewInteraction {
                    override fun onItemSelected(item: Review, position: Int) {
                        //TODO Implement later, profile redirection
                    }

                    override fun onEditClicked(item: Review, position: Int) {
                        if (navController.currentDestination?.id == R.id.reviewFragment) {
                            val navWithAction = ReviewFragmentDirections.actionReviewFragmentToReviewCreateFragment(
                                review = item,
                                contentId = args.contentId,
                                contentExternalId = args.contentExternalId,
                                contentExternalIntId = args.contentExternalIntId,
                                contentType = args.contentType,
                                contentTitle = args.contentTitle,
                            )

                            navController.navigate(navWithAction)
                        }
                    }

                    override fun onDeleteClicked(item: Review, position: Int) {
                        if (confirmDialog != null && confirmDialog?.isShowing == true) {
                            confirmDialog?.dismiss()
                            confirmDialog = null
                        }

                        confirmDialog = context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                            val deleteReviewLiveData = viewModel.deleteReview(IDBody(item.id))

                            deleteReviewLiveData.observe(viewLifecycleOwner) { response ->
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

                                        viewModel.viewModelScope.launch {
                                            reviewAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onLikeClicked(item: Review, position: Int) {
                        val voteLiveData = viewModel.voteReview(IDBody(item.id))

                        voteLiveData.observe(viewLifecycleOwner) { response ->
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

                                    viewModel.viewModelScope.launch {
                                        reviewAdapter?.handleOperation(Operation(response.data.data, position, OperationEnum.Update))
                                    }
                                }
                            }
                        }
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
                }
            )
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
        viewLifecycleOwner.apply {
            viewModel.reviewList.removeObservers(this)
            lifecycleScope.cancel()
        }

        orientationEventListener?.disable()
        orientationEventListener = null

        confirmDialog = null
        reviewAdapter = null
        sortPopupMenu = null
        super.onDestroyView()
    }
}