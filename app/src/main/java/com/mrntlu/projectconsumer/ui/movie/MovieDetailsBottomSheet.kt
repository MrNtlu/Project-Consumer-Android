package com.mrntlu.projectconsumer.ui.movie

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutListBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnButtomSheetClosed
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchListBody
import com.mrntlu.projectconsumer.models.main.userList.UpdateMovieWatchListBody
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.movie.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsBottomSheet(
    private val onBottomSheetClosed: OnButtomSheetClosed<MovieWatchList>,
    private var watchList: MovieWatchList?,
    private val movieId: String,
    private val movieTMDBId: String,
): BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ListBottomSheet"
    }

    //TODO Listen and load, while loading prevent cancelation via isCancelable
    //TODO New loading animation, lottie
    //TODO Paramater, userlist value
    //https://m3.material.io/components/search/overview

    private var _binding: LayoutListBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailsViewModel by viewModels()

    private var bottomSheetState = if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW
    private var bottomSheetOperation = if (watchList == null) BottomSheetOperation.INSERT else BottomSheetOperation.DELETE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutListBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()
        setObservers()
    }

    private fun setUI() {
        binding.layoutViewInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.VIEW)
        binding.layoutEditInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.EDIT)

        if (bottomSheetState == BottomSheetState.VIEW) {
            bottomSheetOperation = BottomSheetOperation.DELETE

            binding.layoutViewInc.apply {
                val userListStatus = Constants.UserListStatus.firstOrNull { it.request == watchList?.status }

                scoreTV.text = watchList?.score?.toString() ?: "*"
                statusTV.text = userListStatus?.name
                timesFinishedCountTV.text = watchList?.timesFinished?.toString()

                val attrColor = when(userListStatus?.request) {
                    Constants.UserListStatus[0].request -> R.attr.statusActiveColor
                    Constants.UserListStatus[1].request -> R.attr.statusFinishedColor
                    else -> R.attr.statusDroppedColor
                }

                val typedValue = TypedValue()
                root.context.theme.resolveAttribute(attrColor, typedValue, true)

                statusColorDivider.dividerColor = ContextCompat.getColor(root.context, typedValue.resourceId)
                timesFinishedTV.setVisibilityByCondition(userListStatus?.request != Constants.UserListStatus[1].request)
                timesFinishedCountTV.setVisibilityByCondition(userListStatus?.request != Constants.UserListStatus[1].request)
            }

            binding.saveButton.text = getString(R.string.edit)
            binding.cancelButton.text = getString(R.string.dismiss)
        } else {
            bottomSheetOperation = BottomSheetOperation.INSERT

            binding.layoutEditInc.apply {
                firstToggleGroupButton.text = Constants.UserListStatus[0].name
                secondToggleGroupButton.text = Constants.UserListStatus[1].name
                thirdToggleGroupButton.text = Constants.UserListStatus[2].name

                watchList?.let {
                    bottomSheetOperation = BottomSheetOperation.UPDATE

                    toggleButtonGroup.check(
                        when (it.status) {
                            Constants.UserListStatus[0].request -> firstToggleGroupButton.id
                            Constants.UserListStatus[1].request -> secondToggleGroupButton.id
                            else -> thirdToggleGroupButton.id
                        }
                    )

                    setACTVSelection(it.score?.plus(1) ?: 0)
                    timesFinishedTextInputET.setText(it.timesFinished.toString())
                }
            }

            binding.saveButton.text = getString(if (watchList == null) R.string.save else R.string.update)
            binding.cancelButton.text = getString(R.string.cancel)
        }
    }

    private fun setListeners() {
        binding.apply {
            layoutViewInc.deleteButton.setOnClickListener {
                watchList = null
                bottomSheetState = BottomSheetState.SUCCESS
                dismiss()
            }

            layoutEditInc.toggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    if (checkedId == R.id.secondToggleGroupButton)
                        layoutEditInc.timesFinishedTextInputET.setText(
                            if (watchList != null && watchList!!.timesFinished > 0) watchList!!.timesFinished.toString() else "1"
                        )
                    else
                        layoutEditInc.timesFinishedTextInputET.text = null

                    layoutEditInc.timesFinishedTextLayout.setVisibilityByCondition(checkedId != R.id.secondToggleGroupButton)
                }
            }

            layoutEditInc.scoreSelectionACTV.setOnDismissListener {
                if (layoutEditInc.scoreSelectionACTV.text.toString().run { isEmpty() || isBlank() }) {
                    setACTVSelection(0)
                }
            }

            layoutEditInc.timesFinishedTextInputET.addTextChangedListener { text ->
                layoutEditInc.timesFinishedTextLayout.error = if (text.isNullOrEmpty()) getString(R.string.input_number) else null
            }

            saveButton.setOnClickListener {
                if (bottomSheetState == BottomSheetState.EDIT) {
                    val score: Int? = layoutEditInc.scoreSelectionACTV.text.toString().toIntOrNull()
                    val status = Constants.UserListStatus[
                            when (layoutEditInc.toggleButtonGroup.checkedButtonId) {
                                R.id.firstToggleGroupButton -> 0
                                R.id.secondToggleGroupButton -> 1
                                R.id.thirdToggleGroupButton -> 2
                                else -> 0
                            }
                    ].request
                    val timesFinished = layoutEditInc.timesFinishedTextInputET.text?.toString()?.toIntOrNull()

                    if (watchList == null) {
                        val watchListBody = MovieWatchListBody(movieId, movieTMDBId, timesFinished, score, status)
                        viewModel.createMovieWatchList(watchListBody)
                    } else {
                        val updateWatchListBody = UpdateMovieWatchListBody(
                            watchList!!.id, watchList!!.score != score,
                            if (watchList!!.timesFinished != timesFinished) timesFinished else null,
                            if (watchList!!.score != score) score else null,
                            if (watchList!!.status != status) status else null
                        )
                        viewModel.updateMovieWatchList(updateWatchListBody)
                    }
                } else {
                    bottomSheetState = BottomSheetState.EDIT
                    setUI()
                }
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            responseCloseButton.setOnClickListener {
                if (bottomSheetState != BottomSheetState.FAILURE)
                    dismiss()
                else {
                    responseStatusLayout.setGone()
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

    private fun setObservers() {
        viewModel.movieWatchList.observe(viewLifecycleOwner) { response ->
            isCancelable = response != NetworkResponse.Loading

            bottomSheetState = when (response) {
                is NetworkResponse.Success -> BottomSheetState.SUCCESS
                is NetworkResponse.Failure -> BottomSheetState.FAILURE
                else -> BottomSheetState.EDIT
            }

            if (response is NetworkResponse.Success) {
                printLog("Response ${response.data.data}")
                watchList = response.data.data
            }

            binding.apply {
                responseStatusLayout.setVisible()

                responseStatusLottie.apply {
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

                responseStatusTV.text = when(response) {
                    is NetworkResponse.Failure -> response.errorMessage
                    NetworkResponse.Loading -> getString(R.string.please_wait_)
                    is NetworkResponse.Success -> "${getString(R.string.successfully)} ${when(bottomSheetOperation){
                        BottomSheetOperation.INSERT -> getString(R.string.created)
                        BottomSheetOperation.UPDATE -> getString(R.string.updated)
                        BottomSheetOperation.DELETE -> getString(R.string.deleted)
                    }}."
                }

                responseCloseButton.setVisibilityByCondition(response == NetworkResponse.Loading)
            }
        }
    }

    override fun onDestroyView() {
        viewModel.movieWatchList.removeObservers(viewLifecycleOwner)

        if (bottomSheetState == BottomSheetState.SUCCESS)
            onBottomSheetClosed.onSuccess(watchList, bottomSheetOperation)

        super.onDestroyView()
        _binding = null
    }
}