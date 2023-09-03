package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.mrntlu.projectconsumer.databinding.CellCarouselBinding
import com.mrntlu.projectconsumer.databinding.CellCarouselErrorBinding
import com.mrntlu.projectconsumer.databinding.CellCarouselShimmerBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class CarouselAdapter<T: ContentModel>(
    private val interaction: Interaction<T>,
    private val isGame: Boolean = false,
    private val itemWidth: Int,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var errorMessage: String? = null
    private var isLoading = true

    private var arrayList: ArrayList<T> = arrayListOf()

    private val radius = 8f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> CarouselErrorViewHolder(CellCarouselErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> CarouselShimmerViewHolder(CellCarouselShimmerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemSlidePreviewHolder<T>(CellCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                val item = arrayList[position]
                (holder as ItemSlidePreviewHolder<T>).bind(item, position, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as CarouselAdapter<T>.CarouselErrorViewHolder).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Loading.value -> {
                (holder as CarouselAdapter<T>.CarouselShimmerViewHolder).setWidthAndRadius(itemWidth, radius)
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

    inner class CarouselShimmerViewHolder(private val binding: CellCarouselShimmerBinding): RecyclerView.ViewHolder(binding.root) {
        fun setWidthAndRadius(width: Int, radius: Float) {
            binding.apply {
                root.layoutParams.width = width

                val shapeAppearanceModelBuilder = ShapeAppearanceModel.Builder().apply {
                    setAllCorners(CornerFamily.ROUNDED, root.context.dpToPxFloat(radius))
                }
                val shapeAppearanceModel = shapeAppearanceModelBuilder.build()
                binding.shimmerCarouselLayout.shapeAppearanceModel = shapeAppearanceModel
            }
        }
    }

    inner class CarouselErrorViewHolder(private val binding: CellCarouselErrorBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(errorMessage: String?, interaction: Interaction<T>) {
            binding.errorText.text = errorMessage

            binding.refreshButton.setOnClickListener { interaction.onErrorRefreshPressed() }
        }
    }

    inner class ItemSlidePreviewHolder<T: ContentModel>(
        private val binding: CellCarouselBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.root.layoutParams.width = itemWidth

            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(radius)

                val shapeAppearanceModelBuilder = ShapeAppearanceModel.Builder().apply {
                    setAllCorners(CornerFamily.ROUNDED, radiusInPx)
                }
                val shapeAppearanceModel = shapeAppearanceModelBuilder.build()
                binding.carouselLayout.shapeAppearanceModel = shapeAppearanceModel

                previewCard.setGone()
                previewGameCV.setVisibilityByCondition(!isGame)

                previewIV.scaleType = ImageView.ScaleType.CENTER_CROP

                previewComposeView.setVisible()
                previewIV.loadWithGlide(item.imageURL, previewCard, previewComposeView) {
                    if (isGame)
                        transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                    else
                        transform(RoundedCorners(radiusInPx.toInt()))
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