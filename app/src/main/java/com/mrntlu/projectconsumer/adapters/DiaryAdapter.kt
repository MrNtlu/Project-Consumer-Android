package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.databinding.CellDiaryBinding
import com.mrntlu.projectconsumer.databinding.CellDiaryHeaderBinding
import com.mrntlu.projectconsumer.databinding.CellDiaryLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.models.main.userList.Log
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToFormattedDateString

@SuppressLint("NotifyDataSetChanged")
class DiaryAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_TYPE = 6

    var isLoading = true

    private val arrayList: ArrayList<Log> = arrayListOf()
    private var headerCount: Int = 0
    private var currentHeaderCount: Int = 0
    private var isPreviousHeader = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingHolder(CellDiaryLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            HEADER_TYPE -> HeaderHolder(CellDiaryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ItemHolder(CellDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            10
        else if (arrayList.isEmpty())
            1
        else
            arrayList.size + headerCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RecyclerViewEnum.View.value) {
            val log = arrayList[position - currentHeaderCount]

            (holder as ItemHolder).binding.apply {
                titleTV.text = log.contentTitle
            }

            isPreviousHeader = false
        } else if (getItemViewType(position) == HEADER_TYPE) {
            val date = arrayList[position - currentHeaderCount].createdAt.convertToFormattedDateString()

            (holder as HeaderHolder).binding.apply {
                dateTV.text = date
            }

            isPreviousHeader = true
            currentHeaderCount += 1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else {
            if (
                position == 0 ||
                (
                        !isPreviousHeader &&
                        arrayList[(position - currentHeaderCount)].createdAt.convertToFormattedDateString() !=
                                arrayList[(position - currentHeaderCount).minus(1)].createdAt.convertToFormattedDateString()
                )
            )
                HEADER_TYPE
            else
                RecyclerViewEnum.View.value
        }
    }

    fun setData(newList: ArrayList<Log>, headerCount: Int) {
        setState(
            if (arrayList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
            else RecyclerViewEnum.View
        )

        if (arrayList.isNotEmpty())
            arrayList.clear()

        this.headerCount = headerCount

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

    inner class LoadingHolder(
        binding: CellDiaryLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class HeaderHolder(
        val binding: CellDiaryHeaderBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ItemHolder(
        val binding: CellDiaryBinding,
    ): RecyclerView.ViewHolder(binding.root)
}