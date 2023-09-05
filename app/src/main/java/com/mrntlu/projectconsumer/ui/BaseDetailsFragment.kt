package com.mrntlu.projectconsumer.ui

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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
import com.mrntlu.projectconsumer.databinding.FragmentAnimeDetailsBinding
import com.mrntlu.projectconsumer.databinding.LayoutUserInteractionBinding
import com.mrntlu.projectconsumer.interfaces.DetailsModel
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.StreamingPlatform
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showLoginRegisterDialog
import com.mrntlu.projectconsumer.viewmodels.main.common.DetailsConsumeLaterViewModel
import java.util.Locale

abstract class BaseDetailsFragment<T>: BaseFragment<T>() {

    protected val detailsConsumeLaterViewModel: DetailsConsumeLaterViewModel by viewModels()

    protected var isResponseFailed = false
    protected lateinit var countryCode: String

    private var consumeLaterDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (binding !is FragmentAnimeDetailsBinding)
            activity?.window?.statusBarColor = Color.TRANSPARENT
        countryCode = sharedViewModel.getCountryCode()
    }

    override fun onStop() {
        super.onStop()
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
        }
    }

    protected fun <WatchList> setLottieUI(
        binding: LayoutUserInteractionBinding,
        details: DetailsModel<WatchList>?,
        createConsumeLater: () -> Unit,
        showBottomSheet: () -> Unit,
    ) {
        binding.watchLaterLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.bookmark else R.raw.bookmark_night)

            setSafeOnClickListener {
                if (sharedViewModel.isNetworkAvailable() && sharedViewModel.isLoggedIn()) {
                    if (details != null && details.consumeLater == null) {
                        createConsumeLater()
                    } else if (details != null) {
                        if (consumeLaterDeleteLiveData != null && consumeLaterDeleteLiveData?.hasActiveObservers() == true)
                            consumeLaterDeleteLiveData?.removeObservers(viewLifecycleOwner)

                        consumeLaterDeleteLiveData = detailsConsumeLaterViewModel.deleteConsumeLater(
                            IDBody(details.consumeLater!!.id)
                        )

                        consumeLaterDeleteLiveData?.observe(viewLifecycleOwner) { response ->
                            handleUserInteractionLoading(response, binding)

                            if (response is NetworkResponse.Success)
                                details.consumeLater = null

                            if (details.consumeLater == null)
                                handleConsumeLaterLottie(
                                    binding,
                                    details.consumeLater == null
                                )

                            if (response is NetworkResponse.Failure)
                                context?.showErrorDialog(response.errorMessage)
                        }
                    }
                } else {
                    if (!sharedViewModel.isNetworkAvailable())
                        context?.showErrorDialog(getString(R.string.no_internet_connection))
                    else {
                        context?.showLoginRegisterDialog(true) {
                            navController.navigate(R.id.action_global_authFragment)
                        }
                    }
                }
            }
        }

        binding.addListLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.like else R.raw.like_night)

            setSafeOnClickListener {
                if (sharedViewModel.isNetworkAvailable() && sharedViewModel.isLoggedIn()) {
                    if (details != null) {
                        showBottomSheet()
                    }
                } else {
                    if (!sharedViewModel.isNetworkAvailable())
                        context?.showErrorDialog(getString(R.string.no_internet_connection))

                    context?.showLoginRegisterDialog(false) {
                        navController.navigate(R.id.action_global_authFragment)
                    }
                }
            }
        }
    }

    protected fun onCountrySpinnerSelected(
        position: Int,
        streaming: List<Streaming>?,
        streamingAdapter: StreamingAdapter?,
        buyAdapter: StreamingAdapter?,
        rentAdapter: StreamingAdapter?,
    ) {
        countryCode = countryList[position].second
        val streamingList = streaming?.firstOrNull { it.countryCode == countryCode }

        streamingAdapter?.setNewList(streamingList?.streamingPlatforms ?: listOf())
        buyAdapter?.setNewList(streamingList?.buyOptions ?: listOf())
        rentAdapter?.setNewList(streamingList?.rentOptions ?: listOf())
    }

    protected fun handleWatchListLottie(binding: LayoutUserInteractionBinding, isDetailsNull: Boolean) {
        binding.addListLottie.apply {

            setMinAndMaxFrame(0, 130)
            frame = if (isDetailsNull) 130 else 0

            if (frame != 0) {
                setMinAndMaxFrame(75, 129)
            } else {
                setMinAndMaxFrame(0, 75)
            }
            playAnimation()
        }
    }

    protected fun handleConsumeLaterLottie(binding: LayoutUserInteractionBinding, isDetailsNull: Boolean) {
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
            userInteractionLoading.setAnimation(R.raw.loading)
            userInteractionLoadingLayout.setVisibilityByCondition(shouldHide = response !is NetworkResponse.Loading)

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

        nestedScrollView.background = GradientDrawable().apply { setColor(nestedScrollView.context.getColorFromAttr(R.attr.mainBackgroundColor)) }
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

            setHasFixedSize(true)

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

            setHasFixedSize(true)

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
        consumeLaterDeleteLiveData?.removeObservers(this)
        super.onDestroyView()
    }
}