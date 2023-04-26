package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.interfaces.LoadingViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer

class LoadingViewHolder(private val binding: CellLoadingBinding): RecyclerView.ViewHolder(binding.root), LoadingViewHolderBind {
    override fun bind(isDarkTheme: Boolean) {
        binding.loadingComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoadingShimmer(isDarkTheme = isDarkTheme) {
                    fillMaxWidth()
                    padding(4.dp)
                }
            }
        }
    }
}