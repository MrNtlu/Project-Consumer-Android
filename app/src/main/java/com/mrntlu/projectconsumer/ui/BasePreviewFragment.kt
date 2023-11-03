package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.setVisibilityByConditionWithAnimation
import com.mrntlu.projectconsumer.utils.smoothScrollToCenteredPosition
import com.mrntlu.projectconsumer.viewmodels.main.common.PreviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BasePreviewFragment<T: ContentModel>: BaseFragment<FragmentPreviewBinding>() {

    private val viewModel: PreviewViewModel by viewModels()

    protected var upcomingAdapter: PreviewAdapter<T>? = null
    protected var topRatedAdapter: PreviewAdapter<T>? = null
    protected var extraAdapter: PreviewAdapter<T>? = null
    private var showCaseAdapter: CarouselAdapter<T>? = null

    private var guideLinePercent = 0.28

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setGuidelineHeight(isGame: Boolean = false) {
        sharedViewModel.windowHeight.observe(viewLifecycleOwner) {
            sharedViewModel.viewModelScope.launch(Dispatchers.Default) {
                val height: WindowSizeClass = it

                val guidePercentFloat = when(height) {
                    WindowSizeClass.EXPANDED -> if (isGame) 0.4f else 0.34f
                    else -> if (isGame) 0.34f else 0.28f
                }

                val defaultGuidePercent = (binding.guideline14.layoutParams as ConstraintLayout.LayoutParams).guidePercent

                withContext(Dispatchers.Main) {
                    if (guidePercentFloat != defaultGuidePercent)
                        binding.guideline14.setGuidelinePercent(guidePercentFloat)
                }

                guideLinePercent = when(height) {
                    WindowSizeClass.EXPANDED -> if (isGame) 0.4 else 0.34
                    else -> if (isGame) 0.34 else 0.28
                }
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
            val rvItemWidth = if (isGame)
                (rvHeight * 16) / 9
            else
                (rvHeight * 2) / 3

            showCaseAdapter = CarouselAdapter(object : Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        viewModel.setScrollPosition(position)
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
                radiusInPx = context.dpToPxFloat(8f)
            )
            setHasFixedSize(true)
            adapter = showCaseAdapter

            if (viewModel.scrollPositionValue() > 0)
                binding.previewShowcaseRV.smoothScrollToCenteredPosition(viewModel.scrollPositionValue())
        }
    }

    protected fun setRecyclerView(
        isRatioDifferent: Boolean = false,
        firstOnItemSelected: (String) -> Unit,
        firstOnRefreshPressed: () -> Unit,
        secondOnItemSelected: (String) -> Unit,
        secondOnRefreshPressed: () -> Unit,
        extraOnItemSelected: (String) -> Unit,
        extraOnRefreshPressed: () -> Unit,
    ) {
        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(
                object: Interaction<T> {
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
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = context.dpToPxFloat(if (isRatioDifferent) 12f else 8f),
            )
            setHasFixedSize(true)
            adapter = upcomingAdapter
        }

        binding.topRatedPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            topRatedAdapter = PreviewAdapter(
                object: Interaction<T> {
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
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = context.dpToPxFloat(if (isRatioDifferent) 12f else 8f),
            )
            setHasFixedSize(true)
            adapter = topRatedAdapter
        }

        binding.extraPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            extraAdapter = PreviewAdapter(
                object: Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        extraOnItemSelected(item.id)
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onErrorRefreshPressed() {
                        extraOnRefreshPressed()
                    }

                    override fun onExhaustButtonPressed() {}
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = context.dpToPxFloat(if (isRatioDifferent) 12f else 8f),
            )
            setHasFixedSize(true)
            adapter = extraAdapter
        }
    }

    protected fun handleObserver(response: NetworkResponse<PreviewResponse<T>>) {
        when(response) {
            is NetworkResponse.Failure -> {
                upcomingAdapter?.setErrorView(response.errorMessage)
                showCaseAdapter?.setErrorView(response.errorMessage)
                topRatedAdapter?.setErrorView(response.errorMessage)
                extraAdapter?.setErrorView(response.errorMessage)
            }
            NetworkResponse.Loading -> {
                upcomingAdapter?.setLoadingView()
                showCaseAdapter?.setLoadingView()
                topRatedAdapter?.setLoadingView()
                extraAdapter?.setLoadingView()
            }
            is NetworkResponse.Success -> {
                upcomingAdapter?.setData(response.data.upcoming)
                showCaseAdapter?.setData(response.data.popular)
                topRatedAdapter?.setData(response.data.top)

                if (!response.data.extra.isNullOrEmpty()) {
                    extraAdapter?.setData(response.data.extra)
                }
                binding.extraPreviewRV.setVisibilityByConditionWithAnimation(response.data.extra.isNullOrEmpty())
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            sharedViewModel.windowHeight.removeObservers(viewLifecycleOwner)
            sharedViewModel.networkStatus.removeObservers(viewLifecycleOwner)
        }
        showCaseAdapter = null
        upcomingAdapter = null
        topRatedAdapter = null
        extraAdapter = null
        super.onDestroyView()
    }
}