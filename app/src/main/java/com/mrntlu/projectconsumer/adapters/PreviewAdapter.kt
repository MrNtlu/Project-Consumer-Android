package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PreviewErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewErrorBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.utils.Constants.DEFAULT_RATIO
import com.mrntlu.projectconsumer.utils.Constants.GAME_RATIO
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class PreviewAdapter<T: ContentModel>(
    private val interaction: Interaction<T>,
    private var isRatioDifferent: Boolean = false,
    private val isDarkTheme: Boolean,
    private var radiusInPx: Float
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var errorMessage: String? = null
    var isLoading = true
    private val sizeMultiplier = 0.8f

    private var arrayList: ArrayList<T> = arrayListOf()

    var shapeAppearanceModel = ShapeAppearanceModel.Builder().apply {
        setBottomLeftCorner(CornerFamily.ROUNDED, radiusInPx)
        setBottomRightCorner(CornerFamily.ROUNDED, radiusInPx)
    }.build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> PreviewErrorViewHolder<T>(CellPreviewErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemPreviewHolder<T>(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemPreviewHolder<T>).bind(arrayList[position], position, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<T>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Loading.value -> {
                (holder as LoadingViewHolder).bind(if (isRatioDifferent) GAME_RATIO else DEFAULT_RATIO, isDarkTheme, true)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null)
            1
        else
            arrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else
            RecyclerViewEnum.View.value
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

    fun setData(newList: List<T>, isRatioDifferent: Boolean = false) {
        if (this.isRatioDifferent != isRatioDifferent) {
            this.isRatioDifferent = isRatioDifferent

            shapeAppearanceModel = ShapeAppearanceModel.Builder().apply {
                setBottomLeftCorner(CornerFamily.ROUNDED, radiusInPx)
                setBottomRightCorner(CornerFamily.ROUNDED, radiusInPx)
            }.build()
        }
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

    inner class ItemPreviewHolder<T: ContentModel>(
        private val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.apply {
                val params = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ).apply {
                    marginStart = 6
                    marginEnd = 6
                }

                binding.root.layoutParams = params

                previewCard.setGone()
                previewShimmerLayout.setVisible()
                previewShimmerCV.radius = radiusInPx
                previewGameCV.setVisibilityByCondition(!isRatioDifferent)

                (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewCard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewShimmerLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"

                if (isRatioDifferent)
                    previewGameCV.shapeAppearanceModel = shapeAppearanceModel

                previewIV.scaleType = if (isRatioDifferent)
                    ImageView.ScaleType.CENTER_CROP
                else
                    ImageView.ScaleType.FIT_XY

                previewIV.loadWithGlide(item.imageURL, previewCard, previewShimmerLayout, sizeMultiplier) {
                    if (isRatioDifferent)
                        transform(CenterCrop(), RoundedCorners((radiusInPx * sizeMultiplier).toInt()))
                    else
                        transform(RoundedCorners((radiusInPx * sizeMultiplier).toInt()))
                }

                previewIV.contentDescription = item.title
                previewTV.text = item.title
                previewGameTitleTV.text = item.title

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}