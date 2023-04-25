package com.mrntlu.projectconsumer.adapters.viewholders

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.interfaces.LoadingViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer

class LoadingPreviewViewHolder(private val binding: CellLoadingBinding): RecyclerView.ViewHolder(binding.root), LoadingViewHolderBind {
    override fun bind(isDarkTheme: Boolean) {
        binding.loadingComposeView.apply {
            val params = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            binding.root.layoutParams = params

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoadingShimmer(isDarkTheme = isDarkTheme) {
                    fillMaxHeight()
                }
            }
        }
    }
}