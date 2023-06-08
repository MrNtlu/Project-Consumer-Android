package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.databinding.CellSeasonBinding
import com.mrntlu.projectconsumer.models.main.tv.Season
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setVisible

class SeasonAdapter(
    private val seasonList: List<Season>
): RecyclerView.Adapter<SeasonAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellSeasonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = seasonList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val season = seasonList[position]

        holder.binding.apply {
            seasonComposeView.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    LoadingShimmer(isDarkTheme = false, roundedCornerSize = 6.dp) {
                        fillMaxHeight()
                    }
                }
            }

            seasonComposeView.setVisible()
            seasonIV.loadWithGlide(season.imageURL, null, seasonComposeView) {
                transform(RoundedCorners(18))
            }
        }
    }

    inner class ItemHolder(
        val binding: CellSeasonBinding
    ): RecyclerView.ViewHolder(binding.root)
}