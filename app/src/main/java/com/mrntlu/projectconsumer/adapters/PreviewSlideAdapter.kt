package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingPreviewViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PreviewErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewErrorBinding
import com.mrntlu.projectconsumer.databinding.CellSlidePreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class PreviewSlideAdapter<T: ContentModel>(
    private val interaction: Interaction<T>,
    private val isRatioDifferent: Boolean = false,
    private val isDarkTheme: Boolean,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var errorMessage: String? = null
    private var isLoading = true

    private var arrayList: ArrayList<T> = arrayListOf()

    private val infiniteItemCount: Int = Int.MAX_VALUE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> PreviewErrorViewHolder<T>(CellPreviewErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingPreviewViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemSlidePreviewHolder<T>(CellSlidePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null)
            1
        else
            infiniteItemCount
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else
            RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dividedPosition = if (arrayList.isEmpty()) position else position % arrayList.size

        when(getItemViewType(dividedPosition)) {
            RecyclerViewEnum.View.value -> {
                val item = arrayList[dividedPosition]
                (holder as ItemSlidePreviewHolder<T>).bind(item, dividedPosition, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<T>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Loading.value -> {
                (holder as LoadingPreviewViewHolder).bind(if (isRatioDifferent) Constants.GAME_RATIO else Constants.DEFAULT_RATIO, isDarkTheme)
            }
        }
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    fun setData(newList: List<T>) {
        setState(RecyclerViewEnum.View)

        if (arrayList.isNotEmpty())
            arrayList.clear()
        arrayList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        when(rvEnum) {
            RecyclerViewEnum.Loading -> {
                isLoading = true
                errorMessage = null
            }
            RecyclerViewEnum.Error -> {
                isLoading = false
            }
            RecyclerViewEnum.View -> {
                isLoading = false
                errorMessage = null
            }
            else -> {}
        }
    }

    inner class ItemSlidePreviewHolder<T: ContentModel>(
        private val binding: CellSlidePreviewBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(8f)

                previewComposeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        LoadingShimmer(
                            aspectRatio = if (isRatioDifferent) Constants.GAME_RATIO else Constants.DEFAULT_RATIO,
                            isDarkTheme = false,
                        ) {
                            fillMaxHeight()
                        }
                    }
                }

                previewCard.setGone()
                previewComposeView.setVisible()
                previewGameCV.setVisibilityByCondition(!isRatioDifferent)

                (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewCard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewComposeView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"

                if (isRatioDifferent)
                    previewGameCV.radius = radiusInPx

                previewIV.scaleType = if (isRatioDifferent)
                    ImageView.ScaleType.CENTER_CROP
                else
                    ImageView.ScaleType.FIT_XY

                previewIV.loadWithGlide(item.imageURL, previewCard, previewComposeView) {
                    if (isRatioDifferent)
                        transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                    else
                        transform(RoundedCorners(radiusInPx.toInt()))
                }

                previewIV.contentDescription = item.title
                previewTV.text = item.title
                previewGameTitleTV.text = item.title

                root.setOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}