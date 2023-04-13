package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.WindowSizeClass

class ActivitySharedViewModel: ViewModel() {
    val windowSize: MutableLiveData<Triple<WindowSizeClass, WindowSizeClass, Float>> by lazy {
        MutableLiveData(Triple(WindowSizeClass.MEDIUM, WindowSizeClass.MEDIUM, 830.0f))
    }

    fun setWindowSize(widthSize: WindowSizeClass, heightSize: WindowSizeClass, height: Float) {
        windowSize.value = Triple(widthSize, heightSize, height)
    }
}