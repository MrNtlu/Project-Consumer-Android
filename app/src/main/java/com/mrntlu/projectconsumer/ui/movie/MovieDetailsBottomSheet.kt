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
import com.mrntlu.projectconsumer.models.main.userlist.MovieWatchListBody
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.movie.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsBottomSheet(
    private val onBottomSheetClosed: OnButtomSheetClosed,
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

    //TODO if userlist already exits, view else edit
    private var bottomSheetState = BottomSheetState.EDIT

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
        binding.firstToggleGroupButton.text = Constants.UserListStatus[0].name
        binding.secondToggleGroupButton.text = Constants.UserListStatus[1].name
        binding.thirdToggleGroupButton.text = Constants.UserListStatus[2].name
    }

    private fun setListeners() {
        binding.apply {
            toggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    if (checkedId == R.id.secondToggleGroupButton)
                        timesFinishedTextInputET.setText("1")
                    else
                        timesFinishedTextInputET.text = null

                    timesFinishedTextLayout.setVisibilityByCondition(checkedId != R.id.secondToggleGroupButton)
                }
            }

            scoreSelectionACTV.setOnDismissListener {
                if (scoreSelectionACTV.text.toString().run { isEmpty() || isBlank() }) {
                    scoreSelectionACTV.setSelection(0)
                    scoreSelectionACTV.setText(scoreSelectionACTV.adapter.getItem(0).toString(), false)
                    scoreSelectionACTV.dismissDropDown()
                }
            }

            timesFinishedTextInputET.addTextChangedListener { text ->
                timesFinishedTextLayout.error = if (text.isNullOrEmpty()) getString(R.string.input_number) else null
            }

            saveButton.setOnClickListener {
                val score: Int? = scoreSelectionACTV.text.toString().toIntOrNull()
                val status = Constants.UserListStatus[
                        when(toggleButtonGroup.checkedButtonId) {
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

            binding.apply {
                responseStatusLayout.setVisible()

                responseStatusLottie.apply {
                    repeatMode = LottieDrawable.RESTART

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
                    NetworkResponse.Loading -> "Please wait..."
                    is NetworkResponse.Success -> response.data.message
                }

                responseCloseButton.setVisibilityByCondition(response == NetworkResponse.Loading)
            }
        }
    }

    override fun onDestroyView() {
        viewModel.movieWatchList.removeObservers(viewLifecycleOwner)

        if (bottomSheetState == BottomSheetState.SUCCESS)
            onBottomSheetClosed.onSuccess(false)

        super.onDestroyView()
        _binding = null
    }
}