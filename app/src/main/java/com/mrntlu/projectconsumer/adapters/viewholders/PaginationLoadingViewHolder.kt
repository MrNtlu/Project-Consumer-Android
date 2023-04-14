package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellPaginationLoadingBinding
import com.mrntlu.projectconsumer.interfaces.PaginationLoadingViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.PaginationLoadingShimmer

class PaginationLoadingViewHolder(private val binding: CellPaginationLoadingBinding): RecyclerView.ViewHolder(binding.root), PaginationLoadingViewHolderBind {
    override fun bind(gridCount: Int, isDarkTheme: Boolean) {
        binding.paginationLoadingComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { 
                PaginationLoadingShimmer(gridCount = gridCount, isDarkTheme = isDarkTheme)
            }
        }
    }
}