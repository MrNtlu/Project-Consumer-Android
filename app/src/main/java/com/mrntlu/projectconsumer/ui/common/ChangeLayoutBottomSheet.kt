package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutChangeLayoutBinding
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeLayoutBottomSheet: BottomSheetDialogFragment() {
    companion object {
        const val TAG = "ChangeLayoutBottomSheet"
    }

    private var _binding: LayoutChangeLayoutBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutChangeLayoutBinding.inflate(inflater, container, false)
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
            if (sharedViewModel.isAltLayout())
                alternativeLayout.isChecked = true
            else
                defaultLayout.isChecked = true

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                sharedViewModel.setLayoutSelection(checkedId == R.id.alternativeLayout)
            }

            val image = "https://image.tmdb.org/t/p/w300/qJ2tW6WMUDux911r6m7haRef0WH.jpg"
            val title = "The Dark Knight"
            val titleOriginal = "The Dark Knight"
            val description = "Batman raises the stakes in his war on crime. With the help of Lt. Jim Gordon and District Attorney Harvey Dent, Batman sets out to dismantle the remaining criminal organizations that plague the streets. The partnership proves to be effective, but they soon find themselves prey to a reign of chaos unleashed by a rising criminal mastermind known to the terrified citizens of Gotham as the Joker."
            val score = "8.5"
            val length = "2h 32m"
            val cornerRadius = root.context.dpToPxFloat(8f)

            defaultInc.apply {
                previewIV.loadWithGlide(image, previewCard, previewShimmerLayout) {
                    transform(CenterCrop(), RoundedCorners(cornerRadius.toInt()))
                }

                root.setOnClickListener {
                    defaultLayout.isChecked = true
                }
            }

            alternativeInc.apply {
                imageInclude.apply {
                    previewIV.loadWithGlide(image, previewCard, previewShimmerLayout) {
                        transform(CenterCrop(), RoundedCorners(cornerRadius.toInt()))
                    }
                }

                titleTV.text = title
                titleOriginalTV.text = titleOriginal
                descriptionTV.text = description
                scoreTV.text = score
                extraInfoTV.text = length

                root.setOnClickListener {
                    alternativeLayout.isChecked = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}