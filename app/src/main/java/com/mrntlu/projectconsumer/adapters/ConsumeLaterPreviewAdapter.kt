package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterPreviewBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class ConsumeLaterPreviewAdapter(
    val interaction: ConsumeLaterInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var errorMessage: String? = null
    private var arrayList: ArrayList<ConsumeLaterResponse> = arrayListOf()

    private fun handleDiffUtil(newList: ArrayList<ConsumeLaterResponse>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toCollection(ArrayList())

        diffResults.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> ErrorViewHolder<ConsumeLaterResponse>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellConsumeLaterEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellConsumeLaterPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (errorMessage != null || arrayList.isEmpty())
            1
        else
            arrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(arrayList[position], position, interaction)
            }
            RecyclerViewEnum.Empty.value -> {
                (holder as EmptyViewHolder).bind()
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<ConsumeLaterResponse>).bind(errorMessage, interaction, true)
            }
        }
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setData(newList: ArrayList<ConsumeLaterResponse>) {
        setState(
            if (arrayList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
            else RecyclerViewEnum.View
        )

        if (arrayList.isEmpty() && newList.size > 0) {
            if (arrayList.isNotEmpty())
                arrayList.clear()

            arrayList.addAll(newList)
            notifyDataSetChanged()
        } else if (arrayList.isEmpty() && newList.isEmpty())
            notifyDataSetChanged()
        else
            handleDiffUtil(newList)
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        when(rvEnum) {
            RecyclerViewEnum.Empty -> {
                errorMessage = null
            }
            RecyclerViewEnum.View -> {
                errorMessage = null
            }
            else -> {}
        }
    }

    fun handleOperation(operation: Operation<ConsumeLaterResponse>) {
        val newList = arrayList.toMutableList()

        when(operation.operationEnum) {
            OperationEnum.Delete -> {
                newList.remove(operation.data)
            }
            OperationEnum.Update -> {
                if (operation.data != null) {
                    val index = newList.indexOfFirst {
                        it == operation.data
                    }
                    newList[index] = operation.data
                }
            }
        }

        handleDiffUtil(newList as ArrayList<ConsumeLaterResponse>)
    }

    inner class EmptyViewHolder(private val binding: CellConsumeLaterEmptyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.discoverButton.setOnClickListener {
                interaction.onDiscoverButtonPressed()
            }
        }
    }

    inner class ItemViewHolder(
        val binding: CellConsumeLaterPreviewBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConsumeLaterResponse, position: Int, interaction: ConsumeLaterInteraction) {
            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(6f)
                val isRatioDifferent = item.contentType == Constants.ContentType.GAME.request

                imageInclude.apply {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewGameCV.setGone()

                    previewIV.scaleType = if (isRatioDifferent)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.content.imageURL, previewCard, previewShimmerLayout) {
                        if (isRatioDifferent)
                            transform(CenterCrop(), GranularRoundedCorners(
                                radiusInPx, radiusInPx, 0f, 0f
                            ))
                        else
                            transform(GranularRoundedCorners(
                                radiusInPx, radiusInPx, 0f, 0f
                            ))
                    }

                    previewCard.radius = radiusInPx
                    previewTV.text = item.content.titleEn
                    previewIV.contentDescription = item.content.titleEn
                }
                consumeLaterCV.radius = radiusInPx
                titleTV.text = item.content.titleEn

                bookmarkButton.setSafeOnClickListener {
                    interaction.onDeletePressed(item, position)
                }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}