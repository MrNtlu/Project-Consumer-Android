package com.mrntlu.projectconsumer.ui.movie

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutListBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnButtomSheetClosed
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchListBody
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
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
        //TODO Set colors, active green, finished blue, dropped red.
        binding.layoutEditInc.firstToggleGroupButton.text = Constants.UserListStatus[0].name
        binding.layoutEditInc.secondToggleGroupButton.text = Constants.UserListStatus[1].name
        binding.layoutEditInc.thirdToggleGroupButton.text = Constants.UserListStatus[2].name
    }

    private fun setListeners() {
        binding.apply {
            layoutEditInc.toggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    if (checkedId == R.id.secondToggleGroupButton)
                        layoutEditInc.timesFinishedTextInputET.setText("1")
                    else
                        layoutEditInc.timesFinishedTextInputET.text = null

                    layoutEditInc.timesFinishedTextLayout.setVisibilityByCondition(checkedId != R.id.secondToggleGroupButton)
                }
            }

            layoutEditInc.scoreSelectionACTV.setOnDismissListener {
                if (layoutEditInc.scoreSelectionACTV.text.toString().run { isEmpty() || isBlank() }) {
                    layoutEditInc.scoreSelectionACTV.setSelection(0)
                    layoutEditInc.scoreSelectionACTV.setText(layoutEditInc.scoreSelectionACTV.adapter.getItem(0).toString(), false)
                    layoutEditInc.scoreSelectionACTV.dismissDropDown()
                }
            }

            layoutEditInc.timesFinishedTextInputET.addTextChangedListener { text ->
                layoutEditInc.timesFinishedTextLayout.error = if (text.isNullOrEmpty()) getString(R.string.input_number) else null
            }

            saveButton.setOnClickListener {
                val score: Int? = layoutEditInc.scoreSelectionACTV.text.toString().toIntOrNull()
                val status = Constants.UserListStatus[
                        when(layoutEditInc.toggleButtonGroup.checkedButtonId) {
                            R.id.firstToggleGroupButton -> 0
                            R.id.secondToggleGroupButton -> 1
                            R.id.thirdToggleGroupButton -> 2
                            else -> 0
                        }
                ].request

                val watchListBody = MovieWatchListBody(movieId, movieTMDBId, score, status)
                viewModel.createMovieWatchList(watchListBody)
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            responseCloseButton.setOnClickListener {
                if (bottomSheetState != BottomSheetState.FAILURE)
                    dismiss()
                else
                    responseStatusLayout.setGone()
            }
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
                watchList = response.data
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

                //TODO Extract strings
                responseStatusTV.text = when(response) {
                    is NetworkResponse.Failure -> response.errorMessage
                    NetworkResponse.Loading -> "Please wait..."
                    is NetworkResponse.Success -> "Successfully created."
                }

                responseCloseButton.setVisibilityByCondition(response == NetworkResponse.Loading)
            }
        }
    }

    override fun onDestroyView() {
        viewModel.movieWatchList.removeObservers(viewLifecycleOwner)

        if (bottomSheetState == BottomSheetState.SUCCESS)
            onBottomSheetClosed.onSuccess(watchList, false)

        super.onDestroyView()
        _binding = null
    }
}