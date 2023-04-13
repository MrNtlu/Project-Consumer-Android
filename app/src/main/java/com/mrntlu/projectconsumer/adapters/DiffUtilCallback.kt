package com.mrntlu.projectconsumer.adapters

import androidx.recyclerview.widget.DiffUtil
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison

class DiffUtilCallback<T: DiffUtilComparison<T>>(
    private val oldList: List<T>,
    private val newList: List<T>,
): DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].areItemsTheSame(newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].areContentsTheSame(newList[newItemPosition])
    }
}