package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.MessageBoxType

class ActivitySharedViewModel: ViewModel() {
    //TODO isLoggedIn, Language Selection

    // Theme
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

    // Window Size
    val windowSize: MutableLiveData<WindowSizeClass> by lazy {
        MutableLiveData(WindowSizeClass.MEDIUM)
    }

    fun setWindowSize(widthSize: WindowSizeClass) {
        windowSize.value = widthSize
    }

    // Network Status
    fun isNetworkAvailable() = networkStatus.value == true

    val networkStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(true)
    }

    fun setNetworkStatus(networkStatus: Boolean) {
        this.networkStatus.value = networkStatus
    }

    // Message Box
    val globalMessageBox: MutableLiveData<Triple<MessageBoxType, String?, Int?>> by lazy {
        MutableLiveData(Triple(MessageBoxType.NOTHING, null, null))
    }

    fun setMessageBox(messageBox: Triple<MessageBoxType, String?, Int?>) {
        globalMessageBox.value = messageBox
    }
}