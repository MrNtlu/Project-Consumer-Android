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
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
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
    private var searchListHolder: ArrayList<ConsumeLaterResponse> = arrayListOf()

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

    fun handleOperation(operation: Operation<ConsumeLaterResponse>) {
        val newList = arrayList.toMutableList()

        when(operation.operationEnum) {
            OperationEnum.Delete -> {
                if (searchListHolder.size > 0)
                    searchListHolder.remove(operation.data)

                newList.remove(operation.data)
            }
            OperationEnum.Update -> {
                if (operation.data != null) {
                    if (searchListHolder.size > 0) {
                        val index = searchListHolder.indexOfFirst {
                            it == operation.data
                        }
                        searchListHolder[index] = operation.data
                    }

                    val index = newList.indexOfFirst {
                        it == operation.data
                    }
                    newList[index] = operation.data
                }
            }
        }

        handleDiffUtil(newList as ArrayList<ConsumeLaterResponse>)
    }

    fun search(search: String?) {
        if (!isLoading && errorMessage == null) {
            if (search?.isNotEmptyOrBlank() == true) {
                val filterList = (
                        if (searchListHolder.size > arrayList.size) searchListHolder
                        else arrayList
                ).toMutableList().toCollection(ArrayList())

                val searchList = filterList.filter {
                    it.content.titleOriginal.startsWith(
                        search,
                        ignoreCase = true
                    ) || it.content.titleOriginal.contains(search, ignoreCase = true)
                }.toMutableList().toCollection(ArrayList())

                if (arrayList.size > searchListHolder.size) {
                    searchListHolder.clear()
                    searchListHolder.addAll(arrayList)
                }

                if (arrayList.size == 0 && searchList.size > 0) {
                    arrayList.addAll(searchList)
                    notifyDataSetChanged()
                } else
                    handleDiffUtil(searchList)
            } else {
                val resetList = arrayList.toMutableList().toCollection(ArrayList())

                resetList.clear()
                resetList.addAll(searchListHolder)
                searchListHolder.clear()

                if (arrayList.size == 0) {
                    arrayList.addAll(resetList)
                    notifyDataSetChanged()
                } else
                    handleDiffUtil(resetList)
            }
        }
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

                    previewTV.text = item.content.titleOriginal
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