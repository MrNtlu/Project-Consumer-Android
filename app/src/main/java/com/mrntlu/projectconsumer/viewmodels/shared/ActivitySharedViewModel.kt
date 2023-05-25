package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.MessageBoxType

class ActivitySharedViewModel: ViewModel() {
    //Is Logged in
    val isAuthenticated: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun isLoggedIn() = isAuthenticated.value == true

    fun setAuthentication(isAuth: Boolean) {
        isAuthenticated.value = isAuth
    }

    //Country Code
    val countryCode: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getCountryCode() = countryCode.value!!

    fun setCountryCode(code: String?) {
        if (code != null)
            this.countryCode.value = code
    }

    //Language Code
    val languageCode: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getLanguageCode() = languageCode.value!!

    fun setLanguageCode(code: String?) {
        if (code != null)
            this.languageCode.value = code
    }

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