package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class PreviewSlideAdapter<T: ContentModel>(
    private val interaction: Interaction<T>,
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
                (holder as LoadingPreviewViewHolder).bind(isDarkTheme)
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
                previewCard.setGone()
                previewIVProgress.setVisible()
                previewIV.loadWithGlide(item.imageURL, previewCard, previewIVProgress) {
                    centerCrop().transform(RoundedCorners(18))
                }

                previewTV.text = item.title

                root.setOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}