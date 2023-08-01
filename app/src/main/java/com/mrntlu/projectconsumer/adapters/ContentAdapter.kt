package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PaginationExhaustViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PaginationLoadingViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPaginationExhaustBinding
import com.mrntlu.projectconsumer.databinding.CellPaginationLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

class ContentAdapter<T: ContentModel>(
    override val interaction: Interaction<T>, gridCount: Int, isDarkTheme: Boolean
): BaseGridPaginationAdapter<T>(interaction, gridCount, isDarkTheme) {

    override fun handleDiffUtil(newList: ArrayList<T>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toList() as ArrayList<T>
        diffResults.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value -> ErrorViewHolder<T>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationLoading.value -> PaginationLoadingViewHolder(CellPaginationLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationExhaust.value -> PaginationExhaustViewHolder(CellPaginationExhaustBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder<T>(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    inner class ItemViewHolder<T: ContentModel>(
        private val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.apply {
                previewComposeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        LoadingShimmer(isDarkTheme = false, roundedCornerSize = 6.dp) {
                            fillMaxHeight()
                        }
                    }
                }

                previewCard.setGone()
                previewComposeView.setVisible()

//                (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (item is Game) "16:9" else "2:3"
//                (previewCard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (item is Game) "16:9" else "2:3"
//                (previewComposeView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (item is Game) "16:9" else "2:3"

                previewIV.loadWithGlide(item.imageURL, previewCard, previewComposeView) {
                    centerCrop().transform(RoundedCorners(24))
                }

                previewTV.text = item.title

                root.setOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}