package com.mrntlu.projectconsumer.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutForgotPasswordBinding
import com.mrntlu.projectconsumer.utils.isEmailValid
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

class AddFriendBottomSheet(
    private val onClicked: (String) -> Unit,
): BottomSheetDialogFragment() {
    constructor(): this({})

    companion object {
        const val TAG = "AddFriendBottomSheet"
    }

    private var _binding: LayoutForgotPasswordBinding? = null
    private val binding get() = _binding!!

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

        binding.apply {
            mailTextLayout.hint = getString(R.string.enter_username_)
            sendButton.text = getString(R.string.search)
        }
        setListeners()
    }

    private fun setListeners() {
        binding.apply {
            sendButton.setSafeOnClickListener {
                if (validate()) {
                    onClicked(mailTextInputET.text!!.toString())
                    dismiss()
                }
            }

            cancelButton.setSafeOnClickListener { dismiss() }
        }
    }

    private fun validate(): Boolean {
        binding.apply {
            mailTextLayout.error = null

            val username = mailTextInputET.text?.toString()

            if (username?.isEmptyOrBlank() == true) {
                mailTextLayout.error = getString(R.string.please_enter_a_valid_username)
                return false
            }
        }
        return true
    }
}