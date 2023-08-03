package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.adapters.PreviewSlideAdapter
import com.mrntlu.projectconsumer.adapters.ProminentLayoutManager
import com.mrntlu.projectconsumer.adapters.decorations.BoundsOffsetDecoration
import com.mrntlu.projectconsumer.adapters.decorations.LinearHorizontalSpacingDecoration
import com.mrntlu.projectconsumer.databinding.FragmentPreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.shared.ViewPagerSharedViewModel

abstract class BasePreviewFragment<T: ContentModel>: BaseFragment<FragmentPreviewBinding>() {

    private val viewPagerSharedViewModel: ViewPagerSharedViewModel by activityViewModels()

    protected var upcomingAdapter: PreviewAdapter<T>? = null
    protected var topRatedAdapter: PreviewAdapter<T>? = null
    private var showCaseAdapter: PreviewSlideAdapter<T>? = null

    private var snapHelper: PagerSnapHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setScrollListener() {
        binding.previewScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            viewPagerSharedViewModel.setScrollYPosition(scrollY)
        }
    }

    protected fun setShowcaseRecyclerView(
        onItemClicked: (String) -> Unit,
        onRefreshPressed: () -> Unit,
    ) {
        binding.previewShowcaseRV.apply {
            val linearLayoutManager = ProminentLayoutManager(context)
            layoutManager = linearLayoutManager

            showCaseAdapter = PreviewSlideAdapter(object: Interaction<T> {
                override fun onItemSelected(item: T, position: Int) {
                    onItemClicked(item.id)
                }

                override fun onErrorRefreshPressed() {
                    onRefreshPressed()
                }

                override fun onCancelPressed() {}

                override fun onExhaustButtonPressed() {}
            }, !sharedViewModel.isLightTheme())
            adapter = showCaseAdapter

            val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
            addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
            addItemDecoration(BoundsOffsetDecoration())

            snapHelper = PagerSnapHelper()
            snapHelper?.attachToRecyclerView(this)

            initRecyclerViewPosition(linearLayoutManager)
        }
    }

    private fun initRecyclerViewPosition(layoutManager: LinearLayoutManager) {
        layoutManager.scrollToPosition(1)

        binding.previewShowcaseRV.doOnPreDraw {
            val targetView = layoutManager.findViewByPosition(1) ?: return@doOnPreDraw
            val distanceToFinalSnap = snapHelper?.calculateDistanceToFinalSnap(layoutManager, targetView) ?: return@doOnPreDraw

            layoutManager.scrollToPositionWithOffset(1, -distanceToFinalSnap[0])
        }
    }

    protected fun setRecyclerView(
        firstOnItemSelected: (String) -> Unit,
        firstOnRefreshPressed: () -> Unit,
        secondOnItemSelected: (String) -> Unit,
        secondOnRefreshPressed: () -> Unit,
    ) {
        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(object: Interaction<T> {
                override fun onItemSelected(item: T, position: Int) {
                    firstOnItemSelected(item.id)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onErrorRefreshPressed() {
                    firstOnRefreshPressed()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = upcomingAdapter
        }

        binding.topRatedPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            topRatedAdapter = PreviewAdapter(object: Interaction<T> {
                override fun onItemSelected(item: T, position: Int) {
                    secondOnItemSelected(item.id)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onErrorRefreshPressed() {
                    secondOnRefreshPressed()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = topRatedAdapter
        }
    }

    protected fun handleObserver(response: NetworkResponse<PreviewResponse<T>>) {
        when(response) {
            is NetworkResponse.Failure -> {
                upcomingAdapter?.setErrorView(response.errorMessage)
                showCaseAdapter?.setErrorView(response.errorMessage)
                topRatedAdapter?.setErrorView(response.errorMessage)
            }
            NetworkResponse.Loading -> {
                upcomingAdapter?.setLoadingView()
                showCaseAdapter?.setLoadingView()
                topRatedAdapter?.setLoadingView()
            }
            is NetworkResponse.Success -> {
                upcomingAdapter?.setData(response.data.upcoming)
                showCaseAdapter?.setData(response.data.popular)
                topRatedAdapter?.setData(response.data.top)
            }
        }
    }

    override fun onDestroyView() {
        sharedViewModel.networkStatus.removeObservers(this)
        binding.previewScrollView.setOnScrollChangeListener(null)
        snapHelper = null
        showCaseAdapter = null
        upcomingAdapter = null
        topRatedAdapter = null
        super.onDestroyView()
    }
}