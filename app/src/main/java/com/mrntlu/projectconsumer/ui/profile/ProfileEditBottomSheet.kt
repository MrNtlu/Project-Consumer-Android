package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.adapters.ProfileImageAdapter
import com.mrntlu.projectconsumer.databinding.LayoutEditProfileBottomSheetBinding
import com.mrntlu.projectconsumer.utils.Constants

class ProfileEditBottomSheet(
    private val selectedImage: String,
    private val onSaveClicked: (String) -> Unit
): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ProfileEditBottomSheet"
    }

    private var _binding: LayoutEditProfileBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var profileImageAdapter: ProfileImageAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutEditProfileBottomSheetBinding.inflate(inflater, container, false)
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
        setRecyclerView()
    }

    private fun setListeners() {
        binding.apply {
            saveButton.setOnClickListener {
                onSaveClicked(
                    profileImageAdapter?.getSelectedImage() ?: Constants.ProfileImageList[0]
                )

                dismiss()
            }

            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun setRecyclerView() {
        binding.profileImageRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout
            profileImageAdapter = ProfileImageAdapter()
            adapter = profileImageAdapter

            profileImageAdapter?.setSelectedImage(selectedImage)
        }
    }

    override fun onDestroyView() {
        profileImageAdapter = null

        super.onDestroyView()
        _binding = null
    }
}