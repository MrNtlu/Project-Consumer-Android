package com.mrntlu.projectconsumer.interfaces

interface OnBottomSheetClosed {
    fun onSuccess(data: UserListModel?, operation: BottomSheetOperation)
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