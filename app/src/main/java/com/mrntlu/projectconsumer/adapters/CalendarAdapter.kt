package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellCalendarBinding
import com.mrntlu.projectconsumer.utils.printLog
import java.time.LocalDate

class CalendarAdapter(
    private val dayList: ArrayList<LocalDate>,
): RecyclerView.Adapter<CalendarAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = dayList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val date = dayList[position]
        holder.binding.dayText.text = date.dayOfMonth.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDays(newList: ArrayList<LocalDate>) {
        dayList.clear()
        dayList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ItemHolder(
        val binding: CellCalendarBinding,
    ): RecyclerView.ViewHolder(binding.root)
}