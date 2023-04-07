package com.mrntlu.projectconsumer.utils

sealed class NetworkListResponse<out T> {
    data class Loading(
        val isPaginating: Boolean = false,
    ): NetworkListResponse<Nothing>()

    data class Success<out T>(
        val data: T,
        val isPaginationData: Boolean = false,
        val isPaginationExhausted: Boolean = false,
    ): NetworkListResponse<T>()

    data class Failure(
        val errorMessage: String,
    ): NetworkListResponse<Nothing>()
}