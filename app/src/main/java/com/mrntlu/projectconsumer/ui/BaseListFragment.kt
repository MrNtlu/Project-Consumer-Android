package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import kotlinx.coroutines.launch

abstract class BaseListFragment<T: ContentModel>: BaseFragment<FragmentListBinding>() {

    protected var contentAdapter: ContentAdapter<T>? = null
    protected var gridCount = 3

    protected var isNavigatingBack = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setToolbar(fetchType: String) {
        binding.listToolbar.apply {
            title = when (fetchType) {
                FetchType.UPCOMING.tag -> getString(R.string.upcoming)
                FetchType.TOP.tag -> getString(R.string.top_rated)
                FetchType.POPULAR.tag -> getString(R.string.popular)
                else -> ""
            }

            setNavigationOnClickListener { navController.popBackStack() }
        }
    }

    protected fun setRecyclerView(
        isRatioDifferent: Boolean = false,
        startFetch: () -> Unit,
        onItemSelected: (String) -> Unit,
        scrollViewModel: (Int) -> Unit,
        fetch: () -> Unit,
    ) {
        binding.listRV.apply {
            val rvLayoutManager = if(sharedViewModel.isAltLayout()) {
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

                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
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
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                isAltLayout = sharedViewModel.isAltLayout(),
                interaction = object: Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        isNavigatingBack = true

                        onItemSelected(item.id)
                    }

                    override fun onErrorRefreshPressed() {
                        startFetch()
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

                    if (isScrolling && !isNavigatingBack) {
                        val centerScrollPosition = (rvLayoutManager.findLastCompletelyVisibleItemPosition() + rvLayoutManager.findFirstCompletelyVisibleItemPosition()) / 2
                        scrollViewModel(centerScrollPosition)
                    }

                    val isScrollingUp = dy <= -90
                    val isScrollingDown = dy >= 10
                    val isThresholdPassed = lastVisibleItemPosition > Constants.PAGINATION_LIMIT.div(gridCount)

                    if (isThresholdPassed && isScrollingUp)
                        binding.topFAB.show()
                    else if (!isThresholdPassed || isScrollingDown)
                        binding.topFAB.hide()

                    contentAdapter?.let {
                        if (
                            isScrolling &&
                            !it.isLoading &&
                            lastVisibleItemPosition >= itemCount.minus(4) &&
                            it.canPaginate &&
                            !it.isPaginating
                        ) {
                            fetch()
                        }
                    }
                }
            })
        }
    }

    protected fun onListObserveHandler(
        response: NetworkListResponse<List<T>>,
        onRestoringAndOrientationChange: () -> Unit
    ) {
        if (response.isFailed()) {
            contentAdapter?.setErrorView(response.errorMessage!!)
        } else if (response.isLoading) {
            contentAdapter?.setLoadingView()
        } else if (response.isSuccessful() || response.isPaginating) {
            val arrayList = response.data!!.toCollection(ArrayList())

            contentAdapter?.setData(
                arrayList,
                response.isPaginationData,
                response.isPaginationExhausted,
                response.isPaginating,
            )

            onRestoringAndOrientationChange()
        }
    }

    protected fun setListeners() {
        binding.topFAB.setOnClickListener {
            binding.listRV.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            sharedViewModel.windowSize.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }
        contentAdapter = null
        super.onDestroyView()
    }
}