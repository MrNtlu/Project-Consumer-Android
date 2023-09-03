package com.mrntlu.projectconsumer.adapters.viewholders

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.interfaces.LoadingViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.Constants.DEFAULT_RATIO
import com.mrntlu.projectconsumer.utils.Constants.GAME_RATIO

class LoadingViewHolder(private val binding: CellLoadingBinding): RecyclerView.ViewHolder(binding.root), LoadingViewHolderBind {
    override fun bind(aspectRatio: Float?, isDarkTheme: Boolean, isPreview: Boolean) {
        binding.loadingComposeView.apply {
            if (isPreview) {
                val params = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                binding.root.layoutParams = params
            }

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoadingShimmer(
                    isDarkTheme = isDarkTheme,
                    aspectRatio = aspectRatio ?: DEFAULT_RATIO,
                    roundedCornerSize = if (isPreview && aspectRatio == GAME_RATIO) 12.dp else 8.dp
                ) {
                    if (isPreview) {
                        fillMaxHeight()
                        padding(horizontal = 3.dp)
                    } else {
                        fillMaxWidth()
                        padding(4.dp)
                    }
                }
            }
        }
    }
}