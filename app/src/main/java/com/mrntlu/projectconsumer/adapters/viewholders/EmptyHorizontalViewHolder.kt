package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellEmptyHorizontalBinding
import com.mrntlu.projectconsumer.utils.dpToPx

class EmptyHorizontalViewHolder(
    private val binding: CellEmptyHorizontalBinding
): RecyclerView.ViewHolder(binding.root) {
    fun setHeight(heightInDp: Float) {
        val height = binding.root.context.dpToPx(heightInDp)

        val layoutParams = binding.root.layoutParams
        layoutParams.height = height
        binding.root.layoutParams = layoutParams
    }
}