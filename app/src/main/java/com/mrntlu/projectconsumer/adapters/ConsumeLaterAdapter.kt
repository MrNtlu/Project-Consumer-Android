package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterBinding
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellConsumeLaterLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
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
                (holder as ErrorViewHolderBind<ConsumeLaterResponse>).bind(errorMessage, interaction)
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

        if (arrayList.isNotEmpty())
            arrayList.clear()

        arrayList.addAll(newList)
        notifyDataSetChanged()
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

    //https://github.com/MrNtlu/RecyclerView-Guide/tree/master/app/src/main/java/com/mrntlu/recyclerviewguide
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
                imageInclude.apply {
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
                    previewIV.loadWithGlide(item.content.imageURL, previewCard, previewComposeView) {
                        transform(RoundedCorners(12))
                    }
                }

                titleTV.text = item.content.titleOriginal
                contentTypeTV.text = Constants.ContentType.fromStringRequest(item.contentType).value

                deleteButton.setOnClickListener {
                    interaction.onDeletePressed(item, position)
                }

                finishedButton.setOnClickListener {
                    interaction.onAddToListPressed(item, position)
                }

                root.setOnClickListener {
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