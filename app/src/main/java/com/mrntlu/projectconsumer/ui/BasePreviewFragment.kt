package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.adapters.PreviewSlideAdapter
import com.mrntlu.projectconsumer.databinding.CellSlideLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellSlidePreviewBinding
import com.mrntlu.projectconsumer.databinding.FragmentPreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.shared.ViewPagerSharedViewModel
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

abstract class BasePreviewFragment<T: ContentModel>: BaseFragment<FragmentPreviewBinding>() {

    private val viewPagerSharedViewModel: ViewPagerSharedViewModel by activityViewModels()

    protected var viewPagerAdapter: PreviewSlideAdapter<T>? = null
    protected var upcomingAdapter: PreviewAdapter<T>? = null
    protected var popularAdapter: PreviewAdapter<T>? = null

    private var pagerListener: CarouselListener? = null

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

    protected fun setLoadingViewPager() {
        binding.loadingViewPager.apply {
            carouselListener = object: CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup
                ): ViewBinding {
                    return CellSlideLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                }

                override fun onBindViewHolder(
                    binding: ViewBinding,
                    item: CarouselItem,
                    position: Int
                ) {
                    val currentBinding = binding as CellSlideLoadingBinding

                    currentBinding.loadingComposeView.apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            LoadingShimmer(isDarkTheme = !sharedViewModel.isLightTheme()) {
                                fillMaxWidth()
                                padding(4.dp)
                            }
                        }
                    }
                }
            }

            val emptyList = arrayListOf<CarouselItem>()
            repeat(10) {
                emptyList.add(CarouselItem())
            }
            setData(emptyList)
        }
    }

    protected fun setViewPager(
        imageList: List<CarouselItem>,
        onClick: (String?) -> Unit
    ) {
        binding.previewViewPager.apply {
            registerLifecycle(viewLifecycleOwner)

            pagerListener = object: CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup
                ): ViewBinding {
                    return CellSlidePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                }

                override fun onBindViewHolder(
                    binding: ViewBinding,
                    item: CarouselItem,
                    position: Int
                ) {
                    val currentBinding = binding as CellSlidePreviewBinding

                    currentBinding.apply {
                        previewCard.setGone()
                        previewIVProgress.setVisible()
                        previewIV.loadWithGlide(item.imageUrl ?: "", previewCard, previewIVProgress) {
                            centerCrop().transform(RoundedCorners(18))
                        }

                        previewTV.text = item.caption

                        root.setOnClickListener {
                            onClick(item.headers?.get("id"))
                        }
                    }
                }
            }
            carouselListener = pagerListener

            setData(imageList)
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

        binding.popularPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            popularAdapter = PreviewAdapter(object: Interaction<T> {
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
            adapter = popularAdapter
        }
    }

    protected fun handleObserver(response: NetworkResponse<DataPaginationResponse<T>>, adapter: PreviewAdapter<T>?) {
        when(response) {
            is NetworkResponse.Failure -> adapter?.setErrorView(response.errorMessage)
            NetworkResponse.Loading -> adapter?.setLoadingView()
            is NetworkResponse.Success -> {
                adapter?.setData(response.data.data)
            }
        }
    }

    override fun onDestroyView() {
        sharedViewModel.networkStatus.removeObservers(this)
        binding.previewScrollView.setOnScrollChangeListener(null)
        pagerListener = null
        upcomingAdapter = null
        popularAdapter = null
        super.onDestroyView()
    }
}