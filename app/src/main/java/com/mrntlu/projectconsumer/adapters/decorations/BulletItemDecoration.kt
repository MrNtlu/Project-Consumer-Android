package com.mrntlu.projectconsumer.adapters.decorations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.utils.getColorFromAttr

class BulletItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val bulletRadius = 4
    private val bulletSpacing = 6

    private val bulletPaint: Paint = Paint()

    init {
        val bulletColor = context.getColorFromAttr(R.attr.mainTextColor)
        bulletPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = bulletColor
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position > 0) {
            outRect.left = bulletSpacing
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val itemCount = parent.childCount

        for (i in 0 until itemCount - 1) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)

            val centerX = (child.right + nextChild.left) / 2f
            val centerY = child.top + child.height / 2f

            c.drawCircle(centerX, centerY, bulletRadius.toFloat(), bulletPaint)
        }
    }
}