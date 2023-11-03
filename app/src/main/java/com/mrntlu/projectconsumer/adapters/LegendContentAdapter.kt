package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyHorizontalViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyHorizontalBinding
import com.mrntlu.projectconsumer.databinding.CellLegendContentBinding
import com.mrntlu.projectconsumer.models.auth.UserInfoCommon
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.capitalizeFirstLetter
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

@SuppressLint("NotifyDataSetChanged")
class LegendContentAdapter(
    private val legendContent: List<UserInfoCommon>,
    private val radiusInPx: Float,
    private val onClick: (UserInfoCommon) -> Unit,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sizeMultiplier = 0.75f
    private val sizeMultipliedRadiusInPx = radiusInPx * sizeMultiplier

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyHorizontalViewHolder(CellEmptyHorizontalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellLegendContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (legendContent.isEmpty())
            1
        else
            legendContent.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (legendContent.isEmpty())
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(legendContent[position], onClick)
            }
            RecyclerViewEnum.Empty.value -> {
                (holder as EmptyHorizontalViewHolder).setHeight(200f)
            }
        }
    }

    inner class ItemViewHolder(
        val binding: CellLegendContentBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserInfoCommon, onClick: (UserInfoCommon) -> Unit) {
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

                    previewIV.loadWithGlide(item.imageURL, previewCard, previewShimmerLayout, sizeMultiplier) {
                        if (isRatioDifferent)
                            transform(CenterCrop(), GranularRoundedCorners(
                                sizeMultipliedRadiusInPx, sizeMultipliedRadiusInPx, 0f, 0f
                            ))
                        else
                            transform(GranularRoundedCorners(
                                sizeMultipliedRadiusInPx, sizeMultipliedRadiusInPx, 0f, 0f
                            ))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = item.title
                }
                legendContentCV.radius = radiusInPx

                val contentType = ContentType.fromStringRequest(item.contentType)

                if (contentType != ContentType.GAME) {
                    val timesFinishedStr = "${item.timesFinished} times"
                    timeFinishedTV.text = timesFinishedStr
                } else {
                    if (item.hoursPlayed != null && item.hoursPlayed > 300) {
                        val hoursPlayed = "${item.hoursPlayed} hrs"
                        timeFinishedTV.text = hoursPlayed
                    } else {
                        val timesFinishedStr = "${item.timesFinished} times"
                        timeFinishedTV.text = timesFinishedStr
                    }
                }

                contentTypeTV.text = if (item.contentType != ContentType.TV.request)
                    item.contentType.capitalizeFirstLetter()
                else
                    item.contentType.uppercase()

                contentTypeIV.setImageDrawable(ContextCompat.getDrawable(
                    root.context,
                    when(contentType) {
                        ContentType.ANIME -> R.drawable.ic_anime
                        ContentType.MOVIE -> R.drawable.ic_content_type_24
                        ContentType.TV -> R.drawable.ic_tv
                        ContentType.GAME -> R.drawable.ic_game_24
                    }
                ))

                root.setOnClickListener {
                    onClick(item)
                }
            }
        }
    }
}