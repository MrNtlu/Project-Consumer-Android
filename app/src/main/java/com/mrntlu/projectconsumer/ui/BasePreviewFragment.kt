package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.HeroCarouselStrategy
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.adapters.CarouselAdapter
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentPreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.common.retrofit.PreviewResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.dpToPx

abstract class BasePreviewFragment<T: ContentModel>: BaseFragment<FragmentPreviewBinding>() {

    protected var upcomingAdapter: PreviewAdapter<T>? = null
    protected var topRatedAdapter: PreviewAdapter<T>? = null
    private var showCaseAdapter: CarouselAdapter<T>? = null

    private var snapHelper: PagerSnapHelper? = null
    private var guideLinePercent = 0.34

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.windowHeight.observe(viewLifecycleOwner) {
            val height: WindowSizeClass = it

            binding.guideline14.setGuidelinePercent(
                when(height) {
                    WindowSizeClass.EXPANDED -> 0.4f
                    else -> 0.34f
                }
            )

            guideLinePercent = when(height) {
                WindowSizeClass.EXPANDED -> 0.4
                else -> 0.34
            }
        }
    }

    protected fun setShowcaseRecyclerView(
        isGame: Boolean = false,
        onItemClicked: (String) -> Unit,
        onRefreshPressed: () -> Unit,
    ) {
        binding.previewShowcaseRV.apply {
            val carouselLayoutManager = if (isGame)
                CarouselLayoutManager(HeroCarouselStrategy())
            else
                CarouselLayoutManager()
            layoutManager = carouselLayoutManager

            val rvHeight = binding.root.context.resources.displayMetrics.heightPixels.times(guideLinePercent).minus(
                binding.root.context.dpToPx(8f)
            )
            val rvItemWidth = if(isGame)
                (rvHeight * 16) / 9
            else
                (rvHeight * 2) / 3

            showCaseAdapter = CarouselAdapter(
                object: Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        onItemClicked(item.id)
                    }

                    override fun onErrorRefreshPressed() {
                        onRefreshPressed()
                    }

                    override fun onCancelPressed() {}

                    override fun onExhaustButtonPressed() {}
                },
                isGame = isGame,
                itemWidth = rvItemWidth.toInt(),
            )
            adapter = showCaseAdapter
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
        isRatioDifferent: Boolean = false,
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
            }, isRatioDifferent = isRatioDifferent, isDarkTheme = !sharedViewModel.isLightTheme())
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
            }, isRatioDifferent = isRatioDifferent, isDarkTheme = !sharedViewModel.isLightTheme())
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
        sharedViewModel.windowHeight.removeObservers(viewLifecycleOwner)
        sharedViewModel.networkStatus.removeObservers(viewLifecycleOwner)
        snapHelper = null
        showCaseAdapter = null
        upcomingAdapter = null
        topRatedAdapter = null
        super.onDestroyView()
    }
}