package com.mrntlu.projectconsumer.viewmodels.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrntlu.projectconsumer.utils.Constants.ContentType

class HomeDiscoverSharedViewModel: ViewModel() {

    val currentContentType: MutableLiveData<ContentType> by lazy {
        MutableLiveData(ContentType.MOVIE)
    }

    fun contentType() = currentContentType.value ?: ContentType.MOVIE

    fun setContentType(newContentType: ContentType) {
        if (currentContentType.value != newContentType)
            this.currentContentType.value = newContentType
    }

    val selectedTabIndex: MutableLiveData<Int> by lazy {
        MutableLiveData(0)
    }

    fun setSelectedTabIndex(newIndex: Int) {
        if (selectedTabIndex.value != newIndex)
            this.selectedTabIndex.value = newIndex
    }
}