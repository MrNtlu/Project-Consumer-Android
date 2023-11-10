package com.mrntlu.projectconsumer.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentReviewCreateBinding
import com.mrntlu.projectconsumer.models.main.review.retrofit.ReviewBody
import com.mrntlu.projectconsumer.models.main.review.retrofit.UpdateReviewBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showSuccessDialog
import com.mrntlu.projectconsumer.viewmodels.main.review.ReviewCreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewCreateFragment : BaseFragment<FragmentReviewCreateBinding>() {

    private val args: ReviewCreateFragmentArgs by navArgs()
    private val viewModel: ReviewCreateViewModel by viewModels()

    private lateinit var dialog: LoadingDialog
    private var confirmDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()
        setUI()
        setListener()
    }

    private fun setToolbar() {
        binding.reviewCreateToolbar.apply {
            setNavigationOnClickListener { navController.popBackStack() }

            title = if (args.review != null)
                getString(R.string._update_review)
            else
                getString(R.string._create_review)

            subtitle = args.contentTitle
        }
    }

    private fun setUI() {
        binding.apply {
            saveButton.text = if (args.review != null) getString(R.string.update) else getString(R.string.post)

            if (args.review != null) {
                ratingBar.rating = args.review!!.star.toFloat()
                reviewET.setText(args.review!!.review)
            }
        }
    }

    private fun setListener() {
        binding.apply {
            reviewLayout.setOnClickListener {
                hideKeyboard()
            }

            reviewET.addTextChangedListener {
                if (reviewTextField.isErrorEnabled && reviewET.length() >= 6)
                    reviewTextField.error = null
            }

            saveButton.setSafeOnClickListener {
                confirmDialog = context?.showConfirmationDialog(getString(R.string.do_you_want_to) + " " + (if (args.review != null) getString(R.string.update) else getString(R.string.post)) + "?") {
                    if (validateFields()) {
                        val liveData = if (args.review != null) {
                            viewModel.updateReview(UpdateReviewBody(
                                args.review!!.id,
                                reviewET.text!!.toString(),
                                ratingBar.rating.toInt()
                            ))
                        } else {
                            viewModel.createReview(ReviewBody(
                                args.contentId,
                                args.contentExternalId,
                                args.contentExternalIntId,
                                args.contentType,
                                ratingBar.rating.toInt(),
                                reviewET.text!!.toString(),
                            ))
                        }

                        liveData.observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showSuccessDialog(getString(R.string.successfully) + " " + (if (args.review != null) getString(R.string.updated) else getString(R.string.posted)) + "🎉") {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    } else {
                        if (ratingBar.rating < 1)
                            ratingBar.rating = 3f
                        else
                            context?.showErrorDialog(getString(R.string.fix_errors))
                    }
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        binding.apply {
            if (ratingBar.rating < 1) {
                context?.showErrorDialog(getString(R.string.review_rating_error))

                return false
            }

            val reviewLength = reviewET.length()
            if (reviewLength > 1000) {
                reviewTextField.error = "Your review can not exceed 1000 characters."
                isValid = false
            } else if (reviewLength < 6) {
                reviewTextField.error = "Your review should be longer than 6 characters."
                isValid = false
            }

            return isValid
        }
    }

    override fun onDestroyView() {
        confirmDialog?.dismiss()
        confirmDialog = null
        super.onDestroyView()
    }
}