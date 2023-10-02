package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

class AnimeRelationContentAdapter(
    private val contentList: List<AnimeRelation>,
    private val onClick: (Int) -> Unit
): RecyclerView.Adapter<AnimeRelationContentAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeRelationContentAdapter.ItemHolder {
        return ItemHolder(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = contentList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val animeDetails = contentList[position]

        holder.binding.apply {
            val radiusInPx = root.context.dpToPxFloat(8f)

            val layoutParams = root.layoutParams
            layoutParams.height = root.context.dpToPx(150f)
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

            previewIV.loadWithGlide(animeDetails.imageURL, previewCard, previewShimmerLayout) {
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