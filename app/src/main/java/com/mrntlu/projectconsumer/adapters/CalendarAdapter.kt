package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellCalendarBinding
import com.mrntlu.projectconsumer.utils.printLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarAdapter(
    private val dayList: ArrayList<LocalDate>,
): RecyclerView.Adapter<CalendarAdapter.ItemHolder>() {

    private val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = dayList.size

    @SuppressLint("UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val date = dayList[position]

        holder.binding.apply {
            val badgeDrawable = BadgeDrawable.create(this.root.context)
            badgeDrawable.number = 3
            badgeDrawable.isVisible = true
            badgeDrawable.backgroundColor = ContextCompat.getColor(this.root.context, R.color.red500)
            badgeDrawable.badgeTextColor = ContextCompat.getColor(this.root.context, R.color.white)

            weekTV.text = date.format(formatter)
            dayTV.text = date.dayOfMonth.toString()

            BadgeUtils.attachBadgeDrawable(badgeDrawable, dayTV)
            printLog("Position $position ${badgeDrawable.isVisible} $date")
        }
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