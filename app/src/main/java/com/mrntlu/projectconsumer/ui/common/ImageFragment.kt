package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.databinding.FragmentImageBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

class ImageFragment : BaseFragment<FragmentImageBinding>() {

    private val args: ImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
    }

    private fun setUI() {
        binding.imageToolbar.setNavigationOnClickListener { navController.popBackStack() }

        binding.imageLayout.apply {
            previewCard.setGone()
            previewShimmerLayout.setVisible()

            (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (args.isRatioDifferent) "16:9" else "2:3"

            previewIV.loadWithGlide(args.imageUrl, previewCard, previewShimmerLayout) {
                transform(CenterCrop())
            }
        }
    }

    override fun onDestroyView() {
        Glide.with(this).clear(binding.imageLayout.previewIV)
        super.onDestroyView()
    }
}