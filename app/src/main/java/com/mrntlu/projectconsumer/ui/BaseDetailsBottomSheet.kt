package com.mrntlu.projectconsumer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutUlViewBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

abstract class BaseDetailsBottomSheet<Binding, WatchList>(
    private val onBottomSheetClosed: OnBottomSheetClosed<WatchList>,
    protected var watchList: WatchList?,
): BottomSheetDialogFragment() {

    protected var _binding: Binding? = null
    protected val binding get() = _binding!!

    protected var bottomSheetState = if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW
    protected var bottomSheetOperation = if (watchList == null) BottomSheetOperation.INSERT else BottomSheetOperation.DELETE

    protected var userListDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()
        setObservers()
    }

    protected abstract fun setUI()
    protected abstract fun setListeners()
    protected abstract fun setObservers()

    protected fun setViewLayout(
        contentType: Constants.ContentType, status: String?, score: Int?, timesFinished: Int?,
        watchedSeasons: Int?, watchedEpisodes: Int?, binding: LayoutUlViewBottomSheetBinding,
    ) {
        binding.apply {
            val userListStatus = Constants.UserListStatus.firstOrNull { it.request == status }

            scoreTV.text = score?.toString() ?: "*"
            statusTV.text = userListStatus?.name
            timesFinishedCountTV.text = timesFinished?.toString() ?: "*"

            val attrColor = when(userListStatus?.request) {
                Constants.UserListStatus[0].request -> R.attr.statusActiveColor
                Constants.UserListStatus[1].request -> R.attr.statusFinishedColor
                else -> R.attr.statusDroppedColor
            }

            val typedValue = TypedValue()
            root.context.theme.resolveAttribute(attrColor, typedValue, true)

            statusColorDivider.dividerColor = ContextCompat.getColor(root.context, typedValue.resourceId)
            statusTV.setTextColor(ContextCompat.getColor(root.context, typedValue.resourceId))

            val shouldHideSeason = contentType == Constants.ContentType.MOVIE || contentType == Constants.ContentType.GAME
            val shouldHideEpisodes = shouldHideSeason || contentType == Constants.ContentType.ANIME

            timesFinishedTV.setVisibilityByCondition(userListStatus?.request != Constants.UserListStatus[1].request)
            timesFinishedCountTV.setVisibilityByCondition(userListStatus?.request != Constants.UserListStatus[1].request)

            seasonTitleTV.setVisibilityByCondition(shouldHideSeason)
            seasonTV.setVisibilityByCondition(shouldHideSeason)

            episodeTitleTV.setVisibilityByCondition(shouldHideEpisodes)
            episodeTV.setVisibilityByCondition(shouldHideEpisodes)

            seasonTV.text = watchedSeasons?.toString() ?: "*"
            episodeTV.text = watchedEpisodes?.toString() ?: "*"
        }
    }

    protected fun setTabLayout(tabLayout: TabLayout) {
        tabLayout.apply {
            for (tab in Constants.UserListStatus) {
                val customTab = newTab().setCustomView(R.layout.cell_status_tab_layout)
                addTab(customTab)
                customTab.customView?.findViewById<TextView>(R.id.tabTV)?.text = tab.name
            }
        }
    }

    protected fun setSelectedTabColors(context: Context, tab: TabLayout.Tab?, tabLayout: TabLayout) {
        val tv = tab?.customView?.findViewById<TextView>(R.id.tabTV)

        val attrColor = when(tv?.text) {
            Constants.UserListStatus[0].name -> R.attr.statusActiveColor
            Constants.UserListStatus[1].name -> R.attr.statusFinishedColor
            else -> R.attr.statusDroppedColor
        }

        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrColor, typedValue, true)

        val color = ContextCompat.getColor(context, typedValue.resourceId)
        tv?.setTextColor(color)
        tabLayout.setSelectedTabIndicatorColor(color)
    }

    protected fun handleBottomSheetState(response: NetworkResponse<*>) {
        isCancelable = response != NetworkResponse.Loading

        bottomSheetState = when(response) {
            is NetworkResponse.Success -> BottomSheetState.SUCCESS
            is NetworkResponse.Failure -> BottomSheetState.FAILURE
            else -> BottomSheetState.VIEW
        }
    }

    protected fun handleResponseStatusLayout(
        view: View,
        lottieAnimationView: LottieAnimationView,
        textView: TextView,
        button: View,
        response: NetworkResponse<*>,
    ) {
        view.setVisible()

        lottieAnimationView.apply {
            repeatMode = LottieDrawable.RESTART

            scaleX = if (response == NetworkResponse.Loading) 1.5f else 1f
            scaleY = if (response == NetworkResponse.Loading) 1.5f else 1f

            setAnimation(
                when(response) {
                    is NetworkResponse.Failure -> R.raw.error
                    NetworkResponse.Loading -> R.raw.loading
                    is NetworkResponse.Success -> R.raw.success
                }
            )

            repeatCount = when(response) {
                is NetworkResponse.Failure, NetworkResponse.Loading -> ValueAnimator.INFINITE
                else -> 0
            }

            playAnimation()
        }

        textView.text = when(response) {
            is NetworkResponse.Failure -> response.errorMessage
            NetworkResponse.Loading -> getString(R.string.please_wait_)
            is NetworkResponse.Success -> "${getString(R.string.successfully)} ${when(bottomSheetOperation){
                BottomSheetOperation.INSERT -> getString(R.string.created)
                BottomSheetOperation.UPDATE -> getString(R.string.updated)
                BottomSheetOperation.DELETE -> getString(R.string.deleted)
            }}."
        }

        button.setVisibilityByCondition(response == NetworkResponse.Loading)
    }

    override fun onDestroyView() {
        userListDeleteLiveData = null

        if (bottomSheetState == BottomSheetState.SUCCESS)
            onBottomSheetClosed.onSuccess(watchList, bottomSheetOperation)

        super.onDestroyView()
        _binding = null
    }
}