package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellFriendRequestBinding
import com.mrntlu.projectconsumer.databinding.CellFriendRequestLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.FriendRequestInteraction
import com.mrntlu.projectconsumer.models.auth.FriendRequest
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class RequestsAdapter(
    private val interaction: FriendRequestInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true

    private var arrayList: ArrayList<FriendRequest> = arrayListOf()

    private fun handleDiffUtil(newList: ArrayList<FriendRequest>) {
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
            RecyclerViewEnum.Error.value -> ErrorViewHolder<FriendRequest>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellFriendRequestLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null || arrayList.isEmpty())
            1
        else
            arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(arrayList[position], position)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<FriendRequest>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Empty.value -> {
                (holder as EmptyViewHolder).changeLottieAnimation(R.raw.notification_empty)
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

    fun setData(newList: List<FriendRequest>) {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        arrayList.addAll(newList)
        setState(RecyclerViewEnum.View)
        notifyDataSetChanged()
    }

    fun handleOperation(operation: Operation<FriendRequest>) {
        val newList = arrayList.toMutableList()

        when(operation.operationEnum) {
            OperationEnum.Delete -> {
                newList.remove(operation.data)
            }
            OperationEnum.Update -> {
                if (operation.data != null) {
                    val index = newList.indexOfFirst {
                        it.id == operation.data.id
                    }
                    newList[index] = operation.data
                }
            }
        }

        handleDiffUtil(newList as ArrayList<FriendRequest>)
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

    inner class ItemViewHolder(
        private val binding: CellFriendRequestBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FriendRequest, position: Int) {
            binding.apply {
                senderImage.loadWithGlide(item.sender.image, null, senderProgressBar) { transform(CenterCrop()) }
                senderTV.text = item.sender.username

                createdAtTV.text = item.createdAt.convertToHumanReadableDateString(true) ?: item.createdAt

                acceptButton.setSafeOnClickListener { interaction.onAcceptClicked(item, position) }

                ignoreButton.setSafeOnClickListener { interaction.onIgnoreClicked(item, position) }

                denyButton.setSafeOnClickListener { interaction.onDenyClicked(item, position) }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }

    inner class LoadingViewHolder(binding: CellFriendRequestLoadingBinding): RecyclerView.ViewHolder(binding.root)
}