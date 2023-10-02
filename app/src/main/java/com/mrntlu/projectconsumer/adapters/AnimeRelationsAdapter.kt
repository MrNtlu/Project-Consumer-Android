package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellAnimeRelationBinding
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation

class AnimeRelationsAdapter(
    private val relationList: List<Pair<String, List<AnimeRelation>>>,
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
            relationTV.text = relation.first

            relationRV.apply {
                val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayoutManager

                val childAdapter = AnimeRelationContentAdapter(
                    relation.second
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