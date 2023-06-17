package com.mrntlu.projectconsumer.utils

data class Operation<out T>(
    val data: T?,
    val position: Int,
    val operationEnum: OperationEnum
)

enum class OperationEnum {
    Delete,
    Update,
}
