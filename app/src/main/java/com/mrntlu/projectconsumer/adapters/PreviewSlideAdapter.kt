package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingPreviewViewHolder
import com.mrntlu.projectconsumer.databinding.CellSlidePreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

class PreviewSlideAdapter<T: ContentModel>(
    private val interaction: Interaction<T>,
    private val isDarkTheme: Boolean,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var errorMessage: String? = null
    var isLoading = false

    private var arrayList: ArrayList<T> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
//            RecyclerViewEnum.Error.value -> PreviewErrorViewHolder<T>(CellPreviewErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//            RecyclerViewEnum.Loading.value -> LoadingPreviewViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemSlidePreviewHolder<T>(CellSlidePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        return RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemSlidePreviewHolder<T>).bind(arrayList[position], position, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<T>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Loading.value -> {
                (holder as LoadingPreviewViewHolder).bind(isDarkTheme)
            }
        }
    }

    fun setData(newList: List<T>) {
//        setState(RecyclerViewEnum.View)

        if (arrayList.isNotEmpty())
            arrayList.clear()
        arrayList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ItemSlidePreviewHolder<T: ContentModel>(
        private val binding: CellSlidePreviewBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            printLog("Called $item")
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