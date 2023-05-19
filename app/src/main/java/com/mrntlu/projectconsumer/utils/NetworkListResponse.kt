package com.mrntlu.projectconsumer.utils

data class NetworkListResponse<T>(
    var data: T?,
    var isLoading: Boolean,
    var isPaginating: Boolean,
    var isFailed: Boolean,
    var isPaginationData: Boolean,
    var isPaginationExhausted: Boolean,
    var errorMessage: String?,
)

fun <T> NetworkListResponse<T>.isSuccessful() = data != null && !isLoading && !isPaginating && !isFailed

fun <T> NetworkListResponse<T>.isFailed() = isFailed && errorMessage != null

fun <T> setLoading() = NetworkListResponse<T>(
    data = null,
    isLoading = true,
    isPaginating = false,
    isFailed = false,
    isPaginationData = false,
    isPaginationExhausted = false,
    errorMessage = null,
)

fun <T> setPaginationLoading(data: T? = null) = NetworkListResponse<T>(
    data = data,
    isLoading = false,
    isPaginating = true,
    isFailed = false,
    isPaginationData = false,
    isPaginationExhausted = false,
    errorMessage = null,
)

fun <T> setFailure(data: T? = null, isPaginationData: Boolean, isPaginationExhausted: Boolean, errorMessage: String) = NetworkListResponse<T>(
    data = data,
    isLoading = false,
    isPaginating = false,
    isFailed = true,
    isPaginationData = isPaginationData,
    isPaginationExhausted = isPaginationExhausted,
    errorMessage = errorMessage,
)

fun <T> setData(data: T, isPaginationData: Boolean, isPaginationExhausted: Boolean) = NetworkListResponse<T>(
    data = data,
    isLoading = false,
    isPaginating = false,
    isFailed = false,
    isPaginationData = isPaginationData,
    isPaginationExhausted = isPaginationExhausted,
    errorMessage = null,
)