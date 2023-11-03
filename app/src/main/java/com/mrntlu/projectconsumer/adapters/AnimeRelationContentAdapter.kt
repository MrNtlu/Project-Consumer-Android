package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

class AnimeRelationContentAdapter(
    private val contentList: List<AnimeRelation>,
    private val radiusInPx: Float,
    private val layoutHeight: Int,
    private val onClick: (Int) -> Unit,
): RecyclerView.Adapter<AnimeRelationContentAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeRelationContentAdapter.ItemHolder {
        return ItemHolder(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = contentList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val animeDetails = contentList[position]

        holder.binding.apply {
            val layoutParams = root.layoutParams
            layoutParams.height = layoutHeight
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

            previewIV.loadWithGlide(animeDetails.imageURL, previewCard, previewShimmerLayout, 0.5f) {
                transform(RoundedCorners(radiusInPx.toInt()))
            }

            root.setSafeOnClickListener {
                if (animeDetails.malID != -1 && animeDetails.type == "anime")
                    onClick(animeDetails.malID)
            }
        }
    }

    inner class ItemHolder(
        val binding: CellPreviewItemBinding
    ): RecyclerView.ViewHolder(binding.root)
}