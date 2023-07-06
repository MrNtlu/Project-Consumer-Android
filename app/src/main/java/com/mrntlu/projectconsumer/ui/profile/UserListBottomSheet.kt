package com.mrntlu.projectconsumer.ui.profile

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutUserListBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisibilityByConditionWithAnimation
import com.mrntlu.projectconsumer.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint

class UserListBottomSheet(
//    private val onBottomSheetClosed: OnBottomSheetClosed<T>,
    private val userListModel: UserListModel?,
    private val contentType: Constants.ContentType,
    private var bottomSheetState: BottomSheetState,
    private val seasonSuffix: Int?,
    private val episodeSuffix: Int?,
): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "UserListBottomSheet"
    }

    private var bottomSheetOperation = if(userListModel == null) BottomSheetOperation.INSERT
        else if (bottomSheetState == BottomSheetState.EDIT) BottomSheetOperation.UPDATE
        else BottomSheetOperation.DELETE

    private var _binding: LayoutUserListBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var userListDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutUserListBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()

        //TODO Create this as basesheet and implement for each usage, because we need viewModels to pass.
        // Or not, just check the viewmodel and if necessary

        //TODO On edit pressed, incrementing the episode and season should be very easy and 1 tap.
    }

    private fun setUI() {
        binding.apply {
            binding.layoutViewInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.VIEW)
            binding.layoutEditInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.EDIT)

            if (bottomSheetState == BottomSheetState.VIEW) {
                bottomSheetOperation = BottomSheetOperation.DELETE

                setViewLayout()

                saveButton.text = getString(R.string.edit)
                cancelButton.text = getString(R.string.dismiss)
            } else if (bottomSheetState == BottomSheetState.EDIT) {
                bottomSheetOperation = BottomSheetOperation.INSERT

                binding.layoutEditInc.apply {
                    setTabLayout(toggleTabLayout)

                    userListModel?.let {
                        toggleTabLayout.getTabAt(
                            when (it.contentStatus) {
                                Constants.UserListStatus[0].request -> 0
                                Constants.UserListStatus[1].request -> 1
                                else -> 2
                            }
                        )?.select()

                        setACTVSelection(userListModel.score?.plus(1) ?: 0)
                        setEditLayout()
                    }

                    setSelectedTabColors(
                        binding.root.context,
                        toggleTabLayout.getTabAt(toggleTabLayout.selectedTabPosition),
                        toggleTabLayout,
                    )
                }
            }

            binding.saveButton.text = getString(if (bottomSheetState == BottomSheetState.EDIT) R.string.save else R.string.update)
            binding.cancelButton.text = getString(R.string.cancel)
        }
    }

    private fun setTabLayout(tabLayout: TabLayout) {
        tabLayout.apply {
            for (tab in Constants.UserListStatus) {
                val customTab = newTab().setCustomView(R.layout.cell_status_tab_layout)
                addTab(customTab)
                customTab.customView?.findViewById<TextView>(R.id.tabTV)?.text = tab.name
            }
        }
    }

    private fun setViewLayout() {
        binding.layoutViewInc.apply {
            userListModel?.let {
                timesFinishedCV.setVisibilityByCondition(it.contentStatus != Constants.UserListStatus[1].request)
                seasonCV.setVisibilityByCondition(contentType != Constants.ContentType.TV)
                episodeCV.setVisibilityByCondition(contentType == Constants.ContentType.MOVIE)

                episodeTitleTV.text = getString(
                    if (contentType == Constants.ContentType.GAME) R.string.hours
                    else R.string.episodes
                )

                val userListStatus = Constants.UserListStatus.firstOrNull { item -> item.request == it.contentStatus }

                scoreTV.text = it.score?.toString() ?: "*"
                statusTV.text = userListStatus?.name
                timesFinishedCountTV.text = it.timesFinished.toString()
                episodeTV.text = it.mainAttribute?.toString() ?: "*"
                seasonTV.text = it.subAttribute?.toString() ?: "*"

                val attrColor = when(userListStatus?.request) {
                    Constants.UserListStatus[0].request -> R.attr.statusActiveColor
                    Constants.UserListStatus[1].request -> R.attr.statusFinishedColor
                    else -> R.attr.statusDroppedColor
                }
                statusColorDivider.dividerColor = root.context.getColorFromAttr(attrColor)
                statusTV.setTextColor(root.context.getColorFromAttr(attrColor))
            }
        }
    }

    private fun setEditLayout() {
        binding.layoutEditInc.apply {
            watchedSeasonTextLayout.setVisibilityByCondition(contentType != Constants.ContentType.TV)
            watchedEpisodeTextLayout.setVisibilityByCondition(contentType == Constants.ContentType.MOVIE)
            watchedEpisodeTextLayout.hint = getString(
                if (contentType == Constants.ContentType.GAME) R.string.hours_played
                else R.string.episodes
            )

            timesFinishedTextInputET.setText(userListModel?.timesFinished.toString())
            watchedSeasonTextInputET.setText(userListModel?.subAttribute.toString())
            watchedEpisodeTextInputET.setText(userListModel?.mainAttribute.toString())

            watchedSeasonTextLayout.suffixText = if (seasonSuffix != null) "/$seasonSuffix" else null
            watchedEpisodeTextLayout.suffixText = if (episodeSuffix != null) "/$episodeSuffix" else null
        }
    }

    private fun setACTVSelection(position: Int) {
        binding.layoutEditInc.scoreSelectionACTV.apply {
            setText(adapter.getItem(position).toString(), false)
            dismissDropDown()
        }
    }

    private fun setSelectedTabColors(context: Context, tab: TabLayout.Tab?, tabLayout: TabLayout) {
        val tv = tab?.customView?.findViewById<TextView>(R.id.tabTV)

        val attrColor = when(tv?.text) {
            Constants.UserListStatus[0].name -> R.attr.statusActiveColor
            Constants.UserListStatus[1].name -> R.attr.statusFinishedColor
            else -> R.attr.statusDroppedColor
        }

        val color = context.getColorFromAttr(attrColor)
        tv?.setTextColor(color)
        tabLayout.setSelectedTabIndicatorColor(color)
    }

    private fun setListeners() {
        binding.apply {
            layoutViewInc.deleteButton.setOnClickListener {
                if (userListModel != null) {
                    if (userListDeleteLiveData != null && userListDeleteLiveData?.hasActiveObservers() == true)
                        userListDeleteLiveData?.removeObservers(viewLifecycleOwner)

                    //TODO delete viewmodel
                }
            }

            layoutEditInc.apply {
                watchedSeasonTextLayout.setEndIconOnClickListener {
                    val currentSeasonNum = watchedEpisodeTextInputET.text?.toString()?.toIntOrNull()
                    watchedEpisodeTextInputET.setText(currentSeasonNum?.plus(1) ?: 1)
                }

                watchedEpisodeTextLayout.setEndIconOnClickListener {
                    val currentEpsNum = watchedEpisodeTextInputET.text?.toString()?.toIntOrNull()
                    watchedEpisodeTextInputET.setText(currentEpsNum?.plus(1) ?: 1)
                }

                scoreSelectionACTV.setOnDismissListener {
                    if (scoreSelectionACTV.text.toString().run { isEmpty() || isBlank() })
                        setACTVSelection(0)
                    else
                        scoreSelectionACTV.clearFocus()
                }

                timesFinishedTextInputET.addTextChangedListener { text ->
                    timesFinishedTextLayout.error = if (text.isNullOrEmpty()) getString(R.string.input_number) else null
                }

                saveButton.setOnClickListener {
                    //TODO Save
                }

                toggleTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        val tv = tab?.customView?.findViewById<TextView>(R.id.tabTV)

                        setSelectedTabColors(
                            binding.root.context,
                            tab,
                            layoutEditInc.toggleTabLayout,
                        )

                        if (tv?.text == Constants.UserListStatus[1].name)
                            layoutEditInc.timesFinishedTextInputET.setText(
                                if (userListModel != null && userListModel.timesFinished > 0) userListModel.timesFinished.toString() else "1"
                            )
                        else
                            layoutEditInc.timesFinishedTextInputET.text = null

                        layoutEditInc.timesFinishedTextLayout.isErrorEnabled = tv?.text == Constants.UserListStatus[1].name
                        layoutEditInc.timesFinishedTextLayout.setVisibilityByConditionWithAnimation(tv?.text != Constants.UserListStatus[1].name)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        tab?.customView?.findViewById<TextView>(R.id.tabTV)?.setTextColor(
                            root.context.getColorFromAttr(R.attr.mainTextColor)
                        )
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })

                cancelButton.setOnClickListener {
                    dismiss()
                }

                responseStatusLayout.responseCloseButton.setOnClickListener {
                    if (bottomSheetState != BottomSheetState.FAILURE)
                        dismiss()
                    else {
                        responseStatusLayout.root.setGone()
                        bottomSheetState = BottomSheetState.EDIT
                    }
                }
            }
        }
    }

    private fun handleBottomSheetState(response: NetworkResponse<*>) {
        isCancelable = response != NetworkResponse.Loading

        bottomSheetState = when(response) {
            is NetworkResponse.Success -> BottomSheetState.SUCCESS
            is NetworkResponse.Failure -> BottomSheetState.FAILURE
            else -> BottomSheetState.VIEW
        }
    }

    private fun handleResponseStatusLayout(
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
        super.onDestroyView()
    }
}