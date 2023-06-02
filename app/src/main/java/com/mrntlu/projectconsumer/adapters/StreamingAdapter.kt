package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellStreamingBinding
import com.mrntlu.projectconsumer.databinding.CellStreamingEmptyBinding
import com.mrntlu.projectconsumer.models.common.StreamingPlatform
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setVisible

class StreamingAdapter(
    private var streamingList: List<StreamingPlatform>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setNewList(newList: List<StreamingPlatform>) {
        streamingList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RecyclerViewEnum.Empty.value -> EmptyHolder(CellStreamingEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ItemHolder(CellStreamingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount() = if (streamingList.isEmpty())
        1
    else streamingList.size

    override fun getItemViewType(position: Int): Int {
        return if (streamingList.isEmpty())
            RecyclerViewEnum.Empty.value
        else RecyclerViewEnum.View.value
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                val item = streamingList[position]
                val itemHolder = holder as ItemHolder

                itemHolder.binding.apply {
                    streamingIVProgressBar.setVisible()
                    streamingIV.loadWithGlide(item.logo, null, streamingIVProgressBar) {
                        centerCrop()
                    }

                    streamingNameTV.text = item.name
                }
            }
        }
    }

    inner class ItemHolder(
        val binding: CellStreamingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class EmptyHolder(
        val binding: CellStreamingEmptyBinding,
    ): RecyclerView.ViewHolder(binding.root)
}