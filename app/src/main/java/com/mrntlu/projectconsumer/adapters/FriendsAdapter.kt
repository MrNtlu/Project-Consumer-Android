package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellFriendBinding
import com.mrntlu.projectconsumer.databinding.CellFriendLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.auth.FriendRequest
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class FriendsAdapter(
    private val interaction: Interaction<BasicUserInfo>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true

    private var arrayList: ArrayList<BasicUserInfo> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> ErrorViewHolder<FriendRequest>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellFriendLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
                (holder as ErrorViewHolderBind<BasicUserInfo>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.Empty.value -> {
                (holder as EmptyViewHolder).changeLottieAnimation(R.raw.no_social)
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

    fun setData(newList: List<BasicUserInfo>) {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        arrayList.addAll(newList)
        setState(RecyclerViewEnum.View)
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

    inner class ItemViewHolder(
        private val binding: CellFriendBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BasicUserInfo, position: Int) {
            binding.apply {
                friendImage.loadWithGlide(item.image ?: "", null, friendProgressBar) { transform(
                    CenterCrop()
                ) }
                friendTV.text = item.username

                premiumAnimation.setVisibilityByCondition(!item.isPremium)

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }

    inner class LoadingViewHolder(binding: CellFriendLoadingBinding): RecyclerView.ViewHolder(binding.root)
}