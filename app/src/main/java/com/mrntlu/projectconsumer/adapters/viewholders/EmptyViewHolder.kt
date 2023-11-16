package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding

class EmptyViewHolder(
    private val binding: CellEmptyBinding
): RecyclerView.ViewHolder(binding.root) {
    fun changeLottieAnimation(newAnimation: Int) {
        binding.emptyLottieAnimation.setAnimation(newAnimation)
    }
}