package com.mrntlu.projectconsumer.ui.tv

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutTvUlBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.models.main.userList.retrofit.DeleteUserListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.TVWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.retrofit.UpdateTVWatchListBody
import com.mrntlu.projectconsumer.ui.BaseDetailsBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisibilityByConditionWithAnimation
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesUserListBottomSheet(
    onBottomSheetClosed: OnBottomSheetClosed<TVSeriesWatchList>,
    watchList: TVSeriesWatchList?,
    private val tvId: String,
    private val tvTMDBId: String,
): BaseDetailsBottomSheet<LayoutTvUlBottomSheetBinding, TVSeriesWatchList>(onBottomSheetClosed, watchList) {

    companion object {
        const val TAG = "TvULBottomSheet"
    }

    private val viewModel: TVDetailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutTvUlBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun setUI() {
        binding.layoutViewInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.VIEW)
        binding.layoutEditInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.EDIT)

        if (bottomSheetState == BottomSheetState.VIEW) {
            bottomSheetOperation = BottomSheetOperation.DELETE

            setViewLayout(
                Constants.ContentType.TV, watchList?.status, watchList?.score,
                watchList?.timesFinished, watchList?.watchedSeasons, watchList?.watchedEpisodes,
                binding.layoutViewInc,
            )

            binding.saveButton.text = getString(R.string.edit)
            binding.cancelButton.text = getString(R.string.dismiss)
        } else {
            bottomSheetOperation = BottomSheetOperation.INSERT

            binding.layoutEditInc.apply {
                setTabLayout(toggleTabLayout)

                watchList?.let {
                    bottomSheetOperation = BottomSheetOperation.UPDATE

                    toggleTabLayout.getTabAt(
                        when (it.status) {
                            Constants.UserListStatus[0].request -> 0
                            Constants.UserListStatus[1].request -> 1
                            else -> 2
                        }
                    )?.select()

                    setACTVSelection(it.score?.plus(1) ?: 0)
                    timesFinishedTextInputET.setText(it.timesFinished.toString())
                    watchedSeasonTextInputET.setText(it.watchedSeasons.toString())
                    watchedEpisodeTextInputET.setText(it.watchedEpisodes.toString())
                }

                setSelectedTabColors(
                    binding.root.context,
                    toggleTabLayout.getTabAt(toggleTabLayout.selectedTabPosition),
                    toggleTabLayout,
                )
            }

            binding.saveButton.text = getString(if (watchList == null) R.string.save else R.string.update)
            binding.cancelButton.text = getString(R.string.cancel)
        }
    }

    override fun setListeners() {
        binding.apply {
            layoutViewInc.deleteButton.setOnClickListener {
                if (watchList != null) {
                    if (userListDeleteLiveData != null && userListDeleteLiveData?.hasActiveObservers() == true)
                        userListDeleteLiveData?.removeObservers(viewLifecycleOwner)

                    userListDeleteLiveData = viewModel.deleteUserList(DeleteUserListBody(watchList!!.id, "movie"))

                    userListDeleteLiveData?.observe(viewLifecycleOwner) { response ->
                        handleBottomSheetState(response)

                        if (response is NetworkResponse.Success) {
                            watchList = null
                        }

                        handleResponseStatusLayout(
                            binding.responseStatusLayout.root,
                            binding.responseStatusLayout.responseStatusLottie,
                            binding.responseStatusLayout.responseStatusTV,
                            binding.responseStatusLayout.responseCloseButton,
                            response
                        )
                    }
                }
            }

            layoutEditInc.toggleTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tv = tab?.customView?.findViewById<TextView>(R.id.tabTV)

                    setSelectedTabColors(
                        binding.root.context,
                        tab,
                        layoutEditInc.toggleTabLayout,
                    )

                    if (tv?.text == Constants.UserListStatus[1].name)
                        layoutEditInc.timesFinishedTextInputET.setText(
                            if (watchList != null && watchList!!.timesFinished > 0) watchList!!.timesFinished.toString() else "1"
                        )
                    else
                        layoutEditInc.timesFinishedTextInputET.text = null

                    layoutEditInc.timesFinishedTextLayout.isErrorEnabled = tv?.text == Constants.UserListStatus[1].name
                    layoutEditInc.timesFinishedTextLayout.setVisibilityByConditionWithAnimation(tv?.text != Constants.UserListStatus[1].name)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    val typedValue = TypedValue()
                    root.context.theme.resolveAttribute(R.attr.mainTextColor, typedValue, true)

                    tab?.customView?.findViewById<TextView>(R.id.tabTV)?.setTextColor(ContextCompat.getColor(binding.root.context, typedValue.resourceId))
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            layoutEditInc.scoreSelectionACTV.setOnDismissListener {
                if (layoutEditInc.scoreSelectionACTV.text.toString().run { isEmpty() || isBlank() })
                    setACTVSelection(0)
                else
                    layoutEditInc.scoreSelectionACTV.clearFocus()
            }

            layoutEditInc.timesFinishedTextInputET.addTextChangedListener { text ->
                layoutEditInc.timesFinishedTextLayout.error = if (text.isNullOrEmpty()) getString(R.string.input_number) else null
            }

            saveButton.setOnClickListener {
                if (bottomSheetState == BottomSheetState.EDIT) {
                    val score: Int? = layoutEditInc.scoreSelectionACTV.text.toString().toIntOrNull()
                    val status = Constants.UserListStatus[layoutEditInc.toggleTabLayout.selectedTabPosition].request
                    val timesFinished = if (status != Constants.UserListStatus[1].request) null
                        else layoutEditInc.timesFinishedTextInputET.text?.toString()?.toIntOrNull()
                    val watchedSeasons = layoutEditInc.watchedSeasonTextInputET.text?.toString()?.toIntOrNull()
                    val watchedEpisodes = layoutEditInc.watchedEpisodeTextInputET.text?.toString()?.toIntOrNull()

                    if (watchList == null) {
                        val tvListBody = TVWatchListBody(tvId, tvTMDBId, timesFinished, watchedEpisodes, watchedSeasons, score, status)
                        viewModel.createTVWatchList(tvListBody)
                    } else {
                        val updateWatchListBody = UpdateTVWatchListBody(
                            watchList!!.id, watchList!!.score != score,
                            if (watchList!!.timesFinished != timesFinished) timesFinished else null,
                            if (watchList!!.watchedEpisodes != watchedEpisodes) watchedEpisodes else null,
                            if (watchList!!.watchedSeasons != watchedSeasons) watchedSeasons else null,
                            if (watchList!!.score != score) score else null,
                            if (watchList!!.status != status) status else null
                        )
                        viewModel.updateTVWatchList(updateWatchListBody)
                    }
                } else {
                    bottomSheetState = BottomSheetState.EDIT
                    setUI()
                }
            }

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

    private fun setACTVSelection(position: Int) {
        binding.layoutEditInc.scoreSelectionACTV.apply {
            setText(adapter.getItem(position).toString(), false)
            dismissDropDown()
        }
    }

    override fun setObservers() {
        viewModel.tvWatchList.observe(viewLifecycleOwner) { response ->
            handleBottomSheetState(response)

            if (response is NetworkResponse.Success) {
                watchList = response.data.data
            }

            handleResponseStatusLayout(
                binding.responseStatusLayout.root,
                binding.responseStatusLayout.responseStatusLottie,
                binding.responseStatusLayout.responseStatusTV,
                binding.responseStatusLayout.responseCloseButton,
                response
            )
        }
    }

    override fun onDestroyView() {
        viewModel.tvWatchList.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}