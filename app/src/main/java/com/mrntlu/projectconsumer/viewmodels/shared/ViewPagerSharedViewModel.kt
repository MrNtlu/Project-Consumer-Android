package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewPagerSharedViewModel: ViewModel() {

    val scrollYPosition: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setScrollYPosition(shouldHide: Int) {
        this.scrollYPosition.value = shouldHide
    }
}