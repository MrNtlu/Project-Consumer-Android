package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellCalendarBinding
import com.mrntlu.projectconsumer.models.common.CalendarUI
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarAdapter(
    private val onClick: (LocalDate) -> Unit,
): RecyclerView.Adapter<CalendarAdapter.ItemHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
    private val calendarUIList: ArrayList<CalendarUI> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = calendarUIList.size

    @SuppressLint("UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val calendarUI = calendarUIList[position]

        holder.binding.apply {
            weekTV.text = calendarUI.date.format(formatter)
            dayTV.text = calendarUI.date.dayOfMonth.toString()

            badgeCV.setVisibilityByCondition(calendarUI.count == 0)

            val count = calendarUI.count
            val countText = if (count > 9) "+9" else count.toString()
            badgeTV.text = countText

            if (calendarUI.count > 0)
                root.setOnClickListener {
                    onClick(calendarUI.date)
                }
            else
                root.setOnClickListener(null)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDays(newList: ArrayList<CalendarUI>) {
        calendarUIList.clear()
        calendarUIList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ItemHolder(
        val binding: CellCalendarBinding,
    ): RecyclerView.ViewHolder(binding.root)
}