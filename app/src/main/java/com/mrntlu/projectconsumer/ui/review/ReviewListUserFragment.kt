package com.mrntlu.projectconsumer.ui.review

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
import com.mrntlu.projectconsumer.adapters.ReviewWithContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ReviewWithContentInteraction
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants.REVIEW_PAGINATION_LIMIT
import com.mrntlu.projectconsumer.utils.Constants.SortReviewRequests
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.viewmodels.main.review.ReviewListUserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReviewListUserFragment : BaseFragment<FragmentListBinding>() {
    private val args: ReviewListUserFragmentArgs by navArgs()

    private var orientationEventListener: OrientationEventListener? = null

    private var confirmDialog: AlertDialog? = null
    private lateinit var dialog: LoadingDialog
    private var sortPopupMenu: PopupMenu? = null
    private var isNavigatingBack = false

    private var reviewWithContentAdapter: ReviewWithContentAdapter? = null

    @Inject
    lateinit var viewModelFactory: ReviewListUserViewModel.Factory
    private val viewModel: ReviewListUserViewModel by viewModels {
        ReviewListUserViewModel.provideReviewListUserViewModelFactory(
            viewModelFactory, this, arguments, args.userId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
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

        setRecyclerView()
        setObservers()
        setListeners()
    }

    private suspend fun isWithinOrientationRange(
        currentOrientation: Int, targetOrientation: Int, epsilon: Int = 30
    ): Boolean = withContext(Dispatchers.Default){
        currentOrientation > targetOrientation - epsilon && currentOrientation < targetOrientation + epsilon
    }

    override fun onStart() {
        super.onStart()

        if (!viewModel.didOrientationChange && viewModel.reviewList.value != null) {
            reviewWithContentAdapter?.setLoadingView()
            viewModel.getReviews(shouldRefresh = true)
        }
    }

    private fun setObservers() {
        viewModel.reviewList.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                reviewWithContentAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                reviewWithContentAdapter?.setLoadingView()
            } else if (response.isSuccessful() || response.isPaginating) {
                val arrayList = response.data!!.toCollection(ArrayList())

                viewModel.viewModelScope.launch {
                    setToolbar(arrayList.first().author.username)

                    reviewWithContentAdapter?.setData(
                        arrayList,
                        response.isPaginationData,
                        response.isPaginationExhausted,
                        response.isPaginating,
                        viewModel.didOrientationChange
                    )
                }

                if (viewModel.didOrientationChange) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)
                    viewModel.didOrientationChange = false
                } else if (!response.isPaginating) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)
                    isNavigatingBack = false
                }
            }
        }
    }

    private fun setToolbar(username: String) {
        binding.listToolbar.apply {
            title = "$username's Reviews"
            inflateMenu(R.menu.sort_toolbar_menu)

            setNavigationOnClickListener { navController.popBackStack() }

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
                                    val sortType = SortReviewRequests[i]

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

                                            SortReviewRequests[0].request
                                        }
                                        R.id.secondSortMenu -> {
                                            setPopupMenuItemVisibility(it, 1)

                                            SortReviewRequests[1].request
                                        }
                                        R.id.thirdSortMenu -> {
                                            setPopupMenuItemVisibility(it, 2)

                                            SortReviewRequests[2].request
                                        }
                                        else -> { SortReviewRequests[0].request }
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
        }
    }

    private fun setListeners() {
        binding.topFAB.setOnClickListener {
            binding.listRV.scrollToPosition(0)
        }
    }

    private fun setPopupMenuItemVisibility(popupMenu: PopupMenu, selectedIndex: Int) {
        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

        for(i in 0..popupMenu.menu.size.minus(1)) {
            popupMenu.menu[i].iconTintList = ContextCompat.getColorStateList(requireContext(), if(i == selectedIndex) selectedColor else unselectedColor)
        }
    }

    private fun setRecyclerView() {
        binding.listRV.apply {
            val linearLayoutManager = LinearLayoutManager(this.context)
            layoutManager = linearLayoutManager

            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
            divider.apply {
                dividerThickness = context.dpToPx(1f)
                isLastItemDecorated = false
            }
            addItemDecoration(divider)

            reviewWithContentAdapter = ReviewWithContentAdapter(
                sharedViewModel.isLoggedIn(),
                context.dpToPxFloat(6f),
                object: ReviewWithContentInteraction {
                    override fun onEditClicked(item: ReviewWithContent, position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onDeleteClicked(item: ReviewWithContent, position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onLikeClicked(item: ReviewWithContent, position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onContentClicked(item: ReviewWithContent, position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onItemSelected(item: ReviewWithContent, position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onErrorRefreshPressed() {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelPressed() {
                        TODO("Not yet implemented")
                    }

                    override fun onExhaustButtonPressed() {
                        TODO("Not yet implemented")
                    }

                }
            )
            adapter = reviewWithContentAdapter

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
                    val isThresholdPassed = lastVisibleItemPosition > REVIEW_PAGINATION_LIMIT

                    if (isThresholdPassed && isScrollingUp)
                        binding.topFAB.show()
                    else if (!isThresholdPassed || isScrollingDown)
                        binding.topFAB.hide()

                    reviewWithContentAdapter?.let {
                        if (
                            isScrolling &&
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(5) &&
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

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.reviewList.removeObservers(this)
            lifecycleScope.cancel()
        }

        orientationEventListener?.disable()
        orientationEventListener = null

        confirmDialog = null
        reviewWithContentAdapter = null
        sortPopupMenu = null
        super.onDestroyView()
    }
}