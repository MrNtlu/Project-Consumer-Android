package com.mrntlu.projectconsumer.interfaces

interface OnButtomSheetClosed<T> {
    fun onSuccess(data: T?, operation: BottomSheetOperation)
}

enum class BottomSheetOperation {
    INSERT,
    UPDATE,
    DELETE
}

enum class BottomSheetState {
    VIEW,
    EDIT,
    SUCCESS,
    FAILURE,
}