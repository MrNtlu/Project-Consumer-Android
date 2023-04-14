package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.utils.Constants

class ActivitySharedViewModel: ViewModel() {
    val themeCode: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun isLightTheme() = themeCode.value == Constants.LIGHT_THEME

    fun setThemeCode(code: Int) {
        themeCode.value = code
    }

    fun toggleTheme() {
        themeCode.value = if (isLightTheme()) {
            Constants.DARK_THEME
        } else {
            Constants.LIGHT_THEME
        }
    }

    val windowSize: MutableLiveData<WindowSizeClass> by lazy {
        MutableLiveData(WindowSizeClass.MEDIUM)
    }

    fun setWindowSize(widthSize: WindowSizeClass) {
        windowSize.value = widthSize
    }
}