package com.mrntlu.projectconsumer.utils

import android.os.SystemClock
import android.view.View

class SafeClickListener(
    private var defaultInterval: Int = 550,
    private val onSafeCLick: (View) -> Unit,
): View.OnClickListener {
    private var lastTimeClicked: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

fun View.setSafeOnClickListener(interval: Int = 550, onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener(interval) {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}