package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellGenreBinding
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

class GenreAdapter(
    private val genreList: List<String>,
    private val onClick: (Int) -> Unit,
): RecyclerView.Adapter<GenreAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = genreList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val genre = genreList[position]

        holder.binding.genreTV.text = genre

        holder.binding.root.setSafeOnClickListener {
            onClick(position)
        }
    }

    inner class ItemHolder(
        val binding: CellGenreBinding
    ): RecyclerView.ViewHolder(binding.root)
}