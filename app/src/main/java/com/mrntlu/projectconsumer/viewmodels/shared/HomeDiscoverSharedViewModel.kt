package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeDiscoverSharedViewModel: ViewModel() {

    val selectedTabIndex: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setSelectedTabIndex(newIndex: Int) {
        if (selectedTabIndex.value != newIndex)
            this.selectedTabIndex.value = newIndex
    }
}