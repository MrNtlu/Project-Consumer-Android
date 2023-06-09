package com.mrntlu.projectconsumer.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellFilterBinding
import com.mrntlu.projectconsumer.models.common.BackendRequestMapper
import com.mrntlu.projectconsumer.utils.getColorFromAttr

class FilterAdapter(
    private val filterList: List<BackendRequestMapper>,
    private val isDeselectable: Boolean = true,
): RecyclerView.Adapter<FilterAdapter.ItemHolder>() {

    private var selectedIndex: Int? = if (isDeselectable) null else 0

    private val borderSize = 6
    private val cornerRadius = 16f
    private val borderColor = R.attr.mainButtonBackgroundColor
    private val backgroundColor = R.attr.subBackgroundColor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = filterList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val text = filterList[position].name

        holder.binding.filterTV.text = text

        addBorder(holder.binding.root.context, holder.binding.filterLayout, selectedIndex == position)

        holder.binding.root.setOnClickListener {
            setSelectedIndex(position)
        }
    }

    private fun addBorder(context: Context, linearLayout: LinearLayout, isSelected: Boolean) {
        val borderDrawable = GradientDrawable()
        if (isSelected)
            borderDrawable.setStroke(borderSize, context.getColorFromAttr(borderColor))
        else
            borderDrawable.setColor(context.getColorFromAttr(backgroundColor))
        borderDrawable.cornerRadius = cornerRadius

        linearLayout.background = borderDrawable
    }

    fun deselect() {
        if (isDeselectable) {
            val oldIndex = selectedIndex

            selectedIndex = null

            if (oldIndex != null)
                notifyItemChanged(oldIndex)
        } else
            setSelectedIndex(0)
    }

    fun setSelectedIndex(newIndex: Int) {
        val oldIndex = selectedIndex

        selectedIndex = if (isDeselectable && selectedIndex == newIndex) null else newIndex

        if (oldIndex != null)
            notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
    }

    fun getSelected() = if (selectedIndex != null) filterList[selectedIndex!!].request else null

    inner class ItemHolder(
        val binding: CellFilterBinding
    ): RecyclerView.ViewHolder(binding.root)
}