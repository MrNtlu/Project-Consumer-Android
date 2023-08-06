package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellAnimeRelationBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelationDetails

class AnimeRelationsAdapter(
    private val relationList: List<AnimeRelation>,
    private val onClick: (Int) -> Unit
): RecyclerView.Adapter<AnimeRelationsAdapter.ItemHolder>() {

    private val viewPool: RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeRelationsAdapter.ItemHolder {
        return ItemHolder(CellAnimeRelationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = relationList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val relation = relationList[position]

        holder.binding.apply {
            relationTV.text = relation.relation

            relationRV.apply {
                val flexboxLayout = FlexboxLayoutManager(context)
                flexboxLayout.apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    alignItems = AlignItems.FLEX_START
                    flexWrap = FlexWrap.WRAP
                }
                layoutManager = flexboxLayout

                val childAdapter = AnimeRelationContentAdapter(
                    relation.source ?:
                    listOf(AnimeRelationDetails(context.getString(R.string.unknown), "", -1, ""))
                ) {
                    onClick(it)
                }
                adapter = childAdapter

                setRecycledViewPool(viewPool)
            }
        }
    }

    inner class ItemHolder(
        val binding: CellAnimeRelationBinding,
    ): RecyclerView.ViewHolder(binding.root)
}