package com.mrntlu.projectconsumer.models.common.retrofit

data class DataPaginationResponse<T>(
    val data: List<T>,
    val pagination: PaginationResponse
)
