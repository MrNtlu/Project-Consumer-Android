package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.databinding.CellDiaryBinding
import com.mrntlu.projectconsumer.databinding.CellDiaryHeaderBinding
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.models.common.LogsUI
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToDateString
import com.mrntlu.projectconsumer.utils.convertToFormattedTime
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

@SuppressLint("NotifyDataSetChanged")
class DiaryAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HEADER_TYPE = 6

    private val arrayList: ArrayList<LogsUI> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            HEADER_TYPE -> HeaderHolder(CellDiaryHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> ItemHolder(CellDiaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (arrayList.isEmpty())
            1
        else
            arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == RecyclerViewEnum.View.value) {
            val log = arrayList[position].log

            (holder as ItemHolder).binding.apply {
                titleTV.text = log.contentTitle
                contentTypeTV.text = Constants.ContentType.fromStringRequest(log.contentType).value
                createdAtTV.text = log.createdAt.convertToFormattedTime()
                actionTypeTV.text = this.root.context.getString(
                    if (log.logAction == "later") {
                        if (log.logType != "game") R.string.watch_later
                        else R.string.play_later
                    } else
                        R.string.user_list
                )
                actionTV.text = log.logAction.replaceFirstChar { it.uppercase() }

                val attrColor = when(log.logAction) {
                    "add" -> R.attr.statusActiveColor
                    "update" -> R.attr.statusFinishedColor
                    else -> R.attr.statusDroppedColor
                }

                actionTV.setTextColor(this.root.context.getColorFromAttr(attrColor))
                diaryDivider.setVisibilityByCondition(position.plus(1) == arrayList.size || arrayList[position.plus(1)].isHeader)
            }
        } else if (getItemViewType(position) == HEADER_TYPE) {
            val date = arrayList[position].log.createdAt.convertToHumanReadableDateString()

            (holder as HeaderHolder).binding.apply {
                dateTV.text = date
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else {
            if (arrayList[position].isHeader)
                HEADER_TYPE
            else
                RecyclerViewEnum.View.value
        }
    }

    fun getScrollPosition(date: String) = arrayList.indexOfFirst {
        it.log.createdAt.convertToDateString() == date && it.isHeader
    }

    fun setData(newList: ArrayList<LogsUI>) {
        if (arrayList.isNotEmpty())
            arrayList.clear()

        arrayList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        notifyDataSetChanged()
    }

    inner class HeaderHolder(
        val binding: CellDiaryHeaderBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ItemHolder(
        val binding: CellDiaryBinding,
    ): RecyclerView.ViewHolder(binding.root)
}