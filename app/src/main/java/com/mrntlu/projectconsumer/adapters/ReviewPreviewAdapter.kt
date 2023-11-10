package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyHorizontalViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyHorizontalBinding
import com.mrntlu.projectconsumer.databinding.CellReviewContentPreviewBinding
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisible

class ReviewPreviewAdapter(
    private val reviewContent: List<ReviewWithContent>,
    private val radiusInPx: Float,
    private val onClick: (ReviewWithContent) -> Unit,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sizeMultiplier = 0.7f
    private val sizeMultipliedRadiusInPx = radiusInPx * sizeMultiplier

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyHorizontalViewHolder(CellEmptyHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellReviewContentPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (reviewContent.isEmpty())
            1
        else
            reviewContent.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (reviewContent.isEmpty())
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(reviewContent[position])
            }
            RecyclerViewEnum.Empty.value -> {
                (holder as EmptyHorizontalViewHolder).setHeight(185f)
            }
        }
    }

    inner class ItemViewHolder(
        val binding: CellReviewContentPreviewBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReviewWithContent) {
            binding.apply {
                val isRatioDifferent = item.contentType == ContentType.GAME.request

                imageInclude.apply {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewTV.setGone()

                    previewIV.scaleType = if (isRatioDifferent)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.content.imageURL, previewCard, previewShimmerLayout, sizeMultiplier) {
                        if (isRatioDifferent)
                            transform(
                                CenterCrop(), GranularRoundedCorners(
                                 0f, 0f, sizeMultipliedRadiusInPx, 0f
                            ))
                        else
                            transform(
                                GranularRoundedCorners(
                                    0f, 0f, sizeMultipliedRadiusInPx, 0f
                            ))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = item.content.titleEn
                }
                contentTitleTV.text = item.content.titleEn

                timeTV.text = item.createdAt.convertToHumanReadableDateString(true) ?: item.createdAt
                reviewRateTV.text = item.star.toString()
                popularityTV.text = item.popularity.toString()
                reviewTV.text = item.review

                likeButton.setImageResource(if (item.isLiked) R.drawable.ic_like else R.drawable.ic_like_outline)
                likeButton.isEnabled = false

                root.setSafeOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}