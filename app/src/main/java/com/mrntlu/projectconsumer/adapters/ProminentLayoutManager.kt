package com.mrntlu.projectconsumer.adapters

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

internal class ProminentLayoutManager(
    context: Context,
    private val minScaleDistanceFactor: Float = 1.5f,
    private val scaleDownBy: Float = 0.5f
) : LinearLayoutManager(context, HORIZONTAL, false) {

    override fun onLayoutCompleted(state: RecyclerView.State?) =
        super.onLayoutCompleted(state).also { scaleChildren() }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) = super.scrollHorizontallyBy(dx, recycler, state).also {
        if (orientation == HORIZONTAL) scaleChildren()
    }

    private fun scaleChildren() {
        val containerCenter = width / 2f

        // Any view further than this threshold will be fully scaled down
        val scaleDistanceThreshold = minScaleDistanceFactor * containerCenter

        var translationXForward = 0f

        for (i in 0 until childCount) {
            val child = getChildAt(i)!!

            val childCenter = (child.left + child.right) / 2f
            val distanceToCenter = abs(childCenter - containerCenter)

            val scaleDownAmount = (distanceToCenter / scaleDistanceThreshold).coerceAtMost(1f)
            val scale = 1f - scaleDownBy * scaleDownAmount

            child.scaleX = scale
            child.scaleY = scale

            val translationDirection = if (childCenter > containerCenter) -1 else 1
            val translationXFromScale = translationDirection * child.width * (1 - scale) / 2f
            child.translationX = translationXFromScale + translationXForward

            translationXForward = 0f

            if (translationXFromScale > 0 && i >= 1) {
                // Edit previous child
                getChildAt(i - 1)!!.translationX += 2 * translationXFromScale

            } else if (translationXFromScale < 0) {
                // Pass on to next child
                translationXForward = 2 * translationXFromScale
            }
        }
    }
}
