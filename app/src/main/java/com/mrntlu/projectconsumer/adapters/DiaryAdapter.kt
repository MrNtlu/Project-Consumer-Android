package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.databinding.CellDiaryBinding
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.models.main.userList.LogsByDate
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum

@SuppressLint("NotifyDataSetChanged")
class DiaryAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isLoading = true

    private val arrayList: ArrayList<LogsByDate> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//            RecyclerViewEnum.Loading.value ->
            else -> ItemHolder(CellDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            10
        else if (arrayList.isEmpty())
            1
        else
            arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RecyclerViewEnum.View.value) {
            (holder as ItemHolder).binding.apply {

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    fun setData(newList: ArrayList<LogsByDate>) {
        setState(
            if (arrayList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
            else RecyclerViewEnum.View
        )

        if (arrayList.isNotEmpty())
            arrayList.clear()

        arrayList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        isLoading = rvEnum == RecyclerViewEnum.Loading
    }

    inner class ItemHolder(
        val binding: CellDiaryBinding,
    ): RecyclerView.ViewHolder(binding.root)
}