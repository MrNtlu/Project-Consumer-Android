package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.databinding.LayoutUserListBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListBottomSheet(

): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "UserListBottomSheet"
    }

    private var _binding: LayoutUserListBottomSheetBinding? = null
    private val binding get() = _binding!!

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

        //TODO Reimplement view and set it for details too
        // get contenttype

        //TODO On edit pressed, incrementing the episode and season should be very easy and 1 tap.
    }

    private fun setListeners() {
        binding.apply {
            layoutEditInc.apply {
                watchedSeasonTextLayout.setEndIconOnClickListener {

                }

                watchedEpisodeTextLayout.setEndIconOnClickListener {

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}