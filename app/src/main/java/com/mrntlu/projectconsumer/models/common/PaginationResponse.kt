package com.mrntlu.projectconsumer.models.common

data class PaginationResponse(
    val next: Int,
    val page: Int,
    val perPage: Int,
    val prev: Int,
    val total: Int,
    val totalPage: Int
) {
    constructor(): this(0,0,0,0,0,0)
}