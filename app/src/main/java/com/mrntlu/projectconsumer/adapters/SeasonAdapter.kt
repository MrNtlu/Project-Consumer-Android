package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.databinding.CellSeasonBinding
import com.mrntlu.projectconsumer.models.main.tv.Season
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

class SeasonAdapter(
    private val seasonList: List<Season>
): RecyclerView.Adapter<SeasonAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellSeasonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = seasonList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val season = seasonList[position]

        holder.binding.apply {
            val radiusInPx = root.context.dpToPxFloat(6f)

            placeHolderCV.setGone()
            previewShimmerLayout.setVisible()
            previewShimmerCV.radius = radiusInPx

            try {
                dateTV.text = season.airDate.convertToHumanReadableDateString(isAlt = true)
            } catch (_: Exception) {
                seasonDateCV.setGone()
            }

            if (season.episodeCount > 0) {
                val episodeStr = "${season.episodeCount} eps."
                episodeCountTV.text = episodeStr
            }
            seasonEpisodeCV.setVisibilityByCondition(season.episodeCount == 0)

            seasonIV.loadWithGlide(season.imageURL, placeHolderCV, previewShimmerLayout) {
                transform(RoundedCorners(radiusInPx.toInt()))
            }

            previewTV.text = season.seasonNum.toString()
            seasonIV.contentDescription = season.name
            seasonEpisodeCV.radius = radiusInPx
            placeHolderCV.radius = radiusInPx
        }
    }

    inner class ItemHolder(
        val binding: CellSeasonBinding
    ): RecyclerView.ViewHolder(binding.root)
}