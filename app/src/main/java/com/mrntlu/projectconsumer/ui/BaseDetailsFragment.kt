package com.mrntlu.projectconsumer.ui

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.LayoutUserInteractionBinding
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.movie.StreamingPlatform
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.viewmodels.main.ConsumeLaterViewModel
import java.util.Locale

abstract class BaseDetailsFragment<T>: BaseFragment<T>() {

    protected val consumeLaterViewModel: ConsumeLaterViewModel by viewModels()

    protected var isResponseFailed = false
    protected lateinit var countryCode: String

    protected var consumeLaterDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    protected val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = Color.TRANSPARENT
        countryCode = sharedViewModel.getCountryCode()
    }

    protected fun handleWatchListLottie(binding: LayoutUserInteractionBinding, isDetailsNull: Boolean) {
        binding.addListLottie.apply {
            frame = if (isDetailsNull) 130 else 0

            if (frame != 0) {
                setMinAndMaxFrame(75, 129)
            } else {
                setMinAndMaxFrame(0, 75)
            }
            playAnimation()
        }
    }

    protected fun handleWatchLaterLottie(binding: LayoutUserInteractionBinding, isDetailsNull: Boolean) {
        binding.watchLaterLottie.apply {
            frame = if (isDetailsNull) 60 else 0

            if (frame != 0) {
                reverseAnimationSpeed()
                setMinAndMaxFrame(0, 60)
            } else {
                speed = 1.4f
                setMinAndMaxFrame(0, 120)
            }
            playAnimation()
        }
    }

    protected fun handleUserInteractionLoading(response: NetworkResponse<*>, binding: LayoutUserInteractionBinding) {
        isResponseFailed = response is NetworkResponse.Failure

        binding.apply {
            userInteractionLoading.setAnimation(if (response is NetworkResponse.Failure) R.raw.error_small else R.raw.loading)
            userInteractionLoadingLayout.setVisibilityByCondition(shouldHide = response is NetworkResponse.Success)

            userInteractionLoading.apply {
                if (response is NetworkResponse.Failure) {
                    repeatCount = 1
                    scaleX = 1.2f
                    scaleY = 1.2f
                } else if (response == NetworkResponse.Loading) {
                    repeatCount = ValueAnimator.INFINITE
                    scaleX = 1.7f
                    scaleY = 1.7f
                }

                if (response != NetworkResponse.Loading)
                    cancelAnimation()
                else
                    playAnimation()
            }
        }
    }

    protected fun onImageFailedHandler(
        collapsingToolbar: CollapsingToolbarLayout,
        nestedScrollView: NestedScrollView,
    ) {
        val params = nestedScrollView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 0
        nestedScrollView.layoutParams = params

        toggleCollapsingLayoutScroll(collapsingToolbar, false)
        nestedScrollView.isNestedScrollingEnabled = false

        val collapsingLayoutParams: AppBarLayout.LayoutParams = collapsingToolbar.layoutParams as AppBarLayout.LayoutParams
        collapsingLayoutParams.scrollFlags = -1
        collapsingToolbar.layoutParams = collapsingLayoutParams
    }

    protected fun setSpinner(spinner: Spinner) {
        val spinnerAdapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, countryList.map { it.first })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.setSelection(
            countryList.indexOfFirst {
                it.second == countryCode
            }
        )
    }

    protected fun createDetailsAdapter(
        recyclerView: RecyclerView, detailsList: List<DetailsUI>,
        placeHolderImage: Int = R.drawable.ic_person_75, cardCornerRadius: Float = 93F,
        transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { centerCrop() },
        setAdapter: (DetailsAdapter) -> DetailsAdapter,
    ) {
        recyclerView.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            adapter = setAdapter(DetailsAdapter(placeHolderImage, cardCornerRadius, detailsList, transformImage))
        }
    }

    protected fun createStreamingAdapter(
        recyclerView: RecyclerView, streamingList: List<StreamingPlatform>?,
        setAdapter: (StreamingAdapter) -> StreamingAdapter,
    ) {
        recyclerView.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            adapter = setAdapter(StreamingAdapter(streamingList ?: listOf()))
        }
    }

    protected fun toggleCollapsingLayoutScroll(collapsingToolbar: CollapsingToolbarLayout, isEnabled: Boolean) {
        collapsingToolbar.apply {
            val collapsingParams = layoutParams as AppBarLayout.LayoutParams
            if (isEnabled)
                collapsingParams.scrollFlags = collapsingParams.scrollFlags or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
            else
                collapsingParams.scrollFlags = collapsingParams.scrollFlags and AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL.inv()

            layoutParams = collapsingParams
        }
    }

    override fun onDestroyView() {
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
        }
        super.onDestroyView()
    }
}