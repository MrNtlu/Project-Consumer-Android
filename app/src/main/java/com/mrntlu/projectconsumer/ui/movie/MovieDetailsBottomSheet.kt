package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.databinding.ListBottomSheetLayoutBinding
import com.mrntlu.projectconsumer.utils.Constants

class MovieDetailsBottomSheet: BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ListBottomSheet"
    }

    //TODO Listen and load, while loading prevent cancelation via isCancelable

    private var _binding: ListBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ListBottomSheetLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()
    }

    private fun setUI() {
        binding.firstToggleGroupButton.text = Constants.UserListStatus[0].name
        binding.secondToggleGroupButton.text = Constants.UserListStatus[1].name
        binding.thirdToggleGroupButton.text = Constants.UserListStatus[2].name
    }

    private fun setListeners() {
        binding.saveButton.setOnClickListener {
            
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}