package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutChangeTabLayoutBinding
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeTabLayoutBottomSheet: BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ChangeTabLayoutBottomSheet"
    }

    private var _binding: LayoutChangeTabLayoutBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutChangeTabLayoutBinding.inflate(inflater, container, false)
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

        setUI()
    }

    private fun setUI() {
        binding.apply {
            if (sharedViewModel.isTabIconsEnabled())
                alternativeLayout.isChecked = true
            else
                defaultLayout.isChecked = true

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                sharedViewModel.setTabLayoutSelection(checkedId == R.id.alternativeLayout)
            }

            val placeHolderList = listOf("First", "Second", "Third")

            defaultInc.tabLayout.apply {
                for (tab in placeHolderList) {
                    addTab(newTab().setText(tab))
                }

                for (position in 0..tabCount.minus(1)) {
                    val layout = LayoutInflater.from(context).inflate(R.layout.layout_tab_title, null) as? LinearLayout

                    val tabIV = layout?.findViewById<ImageView>(R.id.tabIV)
                    val tabLayoutParams = layoutParams
                    tabLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    layoutParams = tabLayoutParams

                    tabIV?.setGone()

                    getTabAt(position)?.customView = layout
                }
            }

            alternativeInc.tabLayout.apply {
                for (tab in placeHolderList) {
                    addTab(newTab().setText(tab))
                }

                for (position in 0..tabCount.minus(1)) {
                    val layout = LayoutInflater.from(context).inflate(R.layout.layout_tab_title, null) as? LinearLayout

                    val tabIV = layout?.findViewById<ImageView>(R.id.tabIV)
                    val tabLayoutParams = layoutParams
                    tabLayoutParams.height = context.dpToPx(65f)
                    layoutParams = tabLayoutParams

                    tabIV?.setImageResource(Constants.TabIconList[position])

                    getTabAt(position)?.customView = layout
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}