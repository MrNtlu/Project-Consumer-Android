package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterBinding
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertLongDateToAgoString
import com.mrntlu.projectconsumer.utils.convertToFormattedDate
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class ConsumeLaterAdapter(
    val interaction: ConsumeLaterInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private var errorMessage: String? = null
    var isLoading = true

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
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellConsumeLaterLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellConsumeLaterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
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

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null || arrayList.isEmpty())
            1
        else
            arrayList.size
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        setState(RecyclerViewEnum.Loading)
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
                isLoading = false
                errorMessage = null
            }
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

    inner class ItemViewHolder(
        private val binding: CellConsumeLaterBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConsumeLaterResponse, position: Int, interaction: ConsumeLaterInteraction) {
            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(6f)

                imageInclude.apply {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewGameCV.setGone()
                    previewTV.setGone()

                    previewIV.scaleType = if (item.contentType == "game")
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.content.imageURL, previewCard, previewShimmerLayout) {
                        if (item.contentType == "game")
                            transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                        else
                            transform(RoundedCorners(radiusInPx.toInt()))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = item.content.titleOriginal
                }

                if (item.content.titleEn.isNotEmptyOrBlank() && item.content.titleEn != item.content.titleOriginal) {
                    titleTV.text = item.content.titleEn
                    titleOriginalTV.text = item.content.titleOriginal
                } else {
                    titleTV.text = item.content.titleOriginal
                    titleOriginalTV.setGone()
                }

                contentTypeTV.text = ContentType.fromStringRequest(item.contentType).value
                scoreTV.text = item.content.score.toDouble().roundSingleDecimal().toString()

                createdAtIV.setVisibilityByCondition(item.createdAt.convertToFormattedDate() == null)
                createdAtTV.setVisibilityByCondition(item.createdAt.convertToFormattedDate() == null)
                createdAtTV.text = item.createdAt.convertToFormattedDate()?.convertLongDateToAgoString()

                contentTypeIV.setImageDrawable(ContextCompat.getDrawable(
                    root.context,
                    when(ContentType.fromStringRequest(item.contentType)) {
                        ContentType.ANIME -> R.drawable.ic_anime
                        ContentType.MOVIE -> R.drawable.ic_content_type_24
                        ContentType.TV -> R.drawable.ic_tv
                        ContentType.GAME -> R.drawable.ic_game_24
                    }
                ))

                actionButton.setSafeOnClickListener {
                    val popupMenu = PopupMenu(root.context, actionButton)
                    popupMenu.menuInflater.inflate(R.menu.consume_later_item_menu, popupMenu.menu)
                    var lastTimeClicked: Long = 0

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        if (SystemClock.elapsedRealtime() - lastTimeClicked < 550) {
                            return@setOnMenuItemClickListener false
                        }
                        lastTimeClicked = SystemClock.elapsedRealtime()

                        when(menuItem.itemId) {
                            R.id.removeMenu -> {
                                interaction.onDeletePressed(item, position)
                            }
                            R.id.finishedMenu -> {
                                interaction.onAddToListPressed(item, position)
                            }
                        }
                        true
                    }

                    popupMenu.show()
                }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }

    inner class EmptyViewHolder(private val binding: CellConsumeLaterEmptyBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.discoverButton.setOnClickListener {
                interaction.onDiscoverButtonPressed()
            }
        }
    }

    inner class LoadingViewHolder(binding: CellConsumeLaterLoadingBinding): RecyclerView.ViewHolder(binding.root)
}