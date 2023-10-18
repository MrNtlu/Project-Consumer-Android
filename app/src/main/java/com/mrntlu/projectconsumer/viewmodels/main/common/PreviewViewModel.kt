package com.mrntlu.projectconsumer.viewmodels.main.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreviewViewModel: ViewModel() {

    val scrollPosition: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun scrollPositionValue() = scrollPosition.value ?: 0

    fun setScrollPosition(position: Int) {
        scrollPosition.value = position
    }
}