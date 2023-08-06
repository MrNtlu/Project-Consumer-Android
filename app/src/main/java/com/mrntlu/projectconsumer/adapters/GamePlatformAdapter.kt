package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellDetailsBinding
import com.mrntlu.projectconsumer.models.common.GamePlatformUI
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.setGone

class GamePlatformAdapter(
    private val platformList: List<GamePlatformUI>,
): RecyclerView.Adapter<GamePlatformAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamePlatformAdapter.ItemHolder {
        return ItemHolder(CellDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = platformList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val platform = platformList[position]

        holder.binding.apply {
            val marginParams = circleIV.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.setMargins(root.context.dpToPx(6f))
            circleIV.layoutParams = marginParams

            ivCard.radius = root.context.dpToPxFloat(12f)

            circleIV.setColorFilter(root.context.getColorFromAttr(R.attr.mainTextColor))
            circleIV.setImageResource(platform.image)
            nameTV.text = platform.requestMapper.name

            circleIVProgressBar.setGone()
            circlePlaceholderIV.setGone()
            subTV.setGone()
        }
    }

    inner class ItemHolder(
        val binding: CellDetailsBinding,
    ): RecyclerView.ViewHolder(binding.root)
}