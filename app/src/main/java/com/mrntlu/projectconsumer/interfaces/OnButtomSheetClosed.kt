package com.mrntlu.projectconsumer.interfaces

interface OnButtomSheetClosed {
    fun onSuccess(isDeleted: Boolean)
}

enum class BottomSheetState {
    VIEW,
    EDIT,
    SUCCESS,
    FAILURE,
}