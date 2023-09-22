package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.models.common.Recommendation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisible

class RecommendationsAdapter(
    private val recommendationList: List<Recommendation>,
    private val onClick: (Int, Recommendation) -> Unit,
): RecyclerView.Adapter<RecommendationsAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = recommendationList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val recommendation = recommendationList[position]

        holder.bind(position, recommendation)
    }

    inner class ItemHolder(
        val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, recommendation: Recommendation) {
            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(8f)

                previewCard.setGone()
                previewShimmerLayout.setVisible()
                previewGameCV.setGone()

                val layoutParams = root.layoutParams
                layoutParams.height = root.context.dpToPx(150f)
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

                previewIV.loadWithGlide(recommendation.imageURL, previewCard, previewShimmerLayout) {
                    transform(RoundedCorners(radiusInPx.toInt()))
                }

                previewIV.contentDescription = recommendation.title
                previewTV.text = recommendation.title
                previewGameTitleTV.text = recommendation.title

                root.setSafeOnClickListener {
                    onClick(position, recommendation)
                }
            }
        }
    }
}