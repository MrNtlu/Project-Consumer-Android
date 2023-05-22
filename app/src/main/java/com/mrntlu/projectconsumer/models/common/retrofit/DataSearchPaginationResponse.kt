package com.mrntlu.projectconsumer.models.common.retrofit

data class DataSearchPaginationResponse<T>(
    val data: List<T>?,
    val pagination: PaginationResponse
)
