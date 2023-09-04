package com.mrntlu.projectconsumer.ui.auth

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutForgotPasswordBinding
import com.mrntlu.projectconsumer.models.auth.retrofit.ForgotPasswordBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isEmailValid
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordBottomSheet: BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ForgotPasswordBottomSheet"
    }

    private var _binding: LayoutForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private var forgotPasswordLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutForgotPasswordBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            (it as? BottomSheetDialog)?.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            sendButton.setOnClickListener {
                if (validate()) {
                    if (forgotPasswordLiveData != null && forgotPasswordLiveData?.hasActiveObservers() == true)
                        forgotPasswordLiveData?.removeObservers(viewLifecycleOwner)

                    forgotPasswordLiveData = viewModel.forgotPassword(ForgotPasswordBody(binding.mailTextInputET.text!!.toString()))

                    forgotPasswordLiveData?.observe(viewLifecycleOwner) { response ->
                        isCancelable = response != NetworkResponse.Loading

                        handleResponseStatusLayout(
                            binding.responseStatusLayout.root,
                            binding.responseStatusLayout.responseStatusLottie,
                            binding.responseStatusLayout.responseStatusTV,
                            binding.responseStatusLayout.responseCloseButton,
                            response,
                        )
                    }
                }
            }

            cancelButton.setOnClickListener {
                dismiss()
            }

            responseStatusLayout.responseCloseButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun validate(): Boolean {
        binding.apply {
            mailTextLayout.error = null

            val email = mailTextInputET.text?.toString()

            if (email?.isEmptyOrBlank() == true) {
                mailTextLayout.error = getString(R.string.please_enter_an_email)
                return false
            } else if (email?.isEmailValid() == false) {
                mailTextLayout.error = getString(R.string.invalid_email_address)
                return false
            }
        }
        return true
    }

    private fun handleResponseStatusLayout(
        view: View,
        lottieAnimationView: LottieAnimationView,
        textView: TextView,
        button: View,
        response: NetworkResponse<MessageResponse>,
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
            is NetworkResponse.Success -> response.data.message
        }

        button.setVisibilityByCondition(response == NetworkResponse.Loading)
    }

    override fun onDestroyView() {
        if (forgotPasswordLiveData != null && forgotPasswordLiveData?.hasActiveObservers() == true)
            forgotPasswordLiveData?.removeObservers(viewLifecycleOwner)

        forgotPasswordLiveData = null

        super.onDestroyView()
        _binding = null
    }
}