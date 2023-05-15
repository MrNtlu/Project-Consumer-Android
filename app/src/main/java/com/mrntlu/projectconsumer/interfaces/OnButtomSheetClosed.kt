package com.mrntlu.projectconsumer.interfaces

interface OnButtomSheetClosed<T> {
    fun onSuccess(data: T?, isDeleted: Boolean)
}

enum class BottomSheetState {
    VIEW,
    EDIT,
    SUCCESS,
    FAILURE,
}