package com.mrntlu.projectconsumer.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellNameUrlBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeNameURL
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

class NameUrlAdapter(
    private val nameUrlList: List<AnimeNameURL>,
): RecyclerView.Adapter<NameUrlAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameUrlAdapter.ItemHolder {
        return ItemHolder(CellNameUrlBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = nameUrlList.count()

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val nameUrl = nameUrlList[position]

        holder.binding.apply {
            nameTV.paintFlags = nameTV.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            nameTV.text = nameUrl.name

            bulletTV.setVisibilityByCondition(itemCount <= 1 || position == itemCount.minus(1))

            root.setOnClickListener {
                if (URLUtil.isValidUrl(nameUrl.url))
                    it.context?.openInBrowser(nameUrl.url)
            }
        }
    }

    inner class ItemHolder(
        val binding: CellNameUrlBinding
    ): RecyclerView.ViewHolder(binding.root)
}