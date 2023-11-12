package com.mrntlu.projectconsumer.adapters.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class OverlapDecoration : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemPosition = parent.getChildAdapterPosition(view)
        if (itemPosition == 0) {
            outRect.set(0, 0, 0, 0)
        } else {
            outRect.set(overlap, 0, 0, 0)
        }
    }

    companion object {
        private const val overlap = -32
    }
}