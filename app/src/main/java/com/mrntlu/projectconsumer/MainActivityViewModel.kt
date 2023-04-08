package com.mrntlu.projectconsumer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.utils.Constants

class MainActivityViewModel: ViewModel() {
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
}