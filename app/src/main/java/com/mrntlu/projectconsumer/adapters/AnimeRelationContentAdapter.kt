package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellAnimeRelationContentBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelationDetails
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

class AnimeRelationContentAdapter(
    private val contentList: List<AnimeRelationDetails>,
    private val onClick: (Int) -> Unit
): RecyclerView.Adapter<AnimeRelationContentAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeRelationContentAdapter.ItemHolder {
        return ItemHolder(CellAnimeRelationContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = contentList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val animeDetails = contentList[position]

        holder.binding.apply {
            animeNameTV.text = animeDetails.name

            bulletTV.setVisibilityByCondition(itemCount <= 1 || position == itemCount.minus(1))

            root.setOnClickListener {
                if (animeDetails.malID != -1 && animeDetails.type == "anime")
                    onClick(animeDetails.malID)
                else if (URLUtil.isValidUrl(animeDetails.redirectURL))
                    it.context.openInBrowser(animeDetails.redirectURL)
            }
        }
    }

    inner class ItemHolder(
        val binding: CellAnimeRelationContentBinding
    ): RecyclerView.ViewHolder(binding.root)
}