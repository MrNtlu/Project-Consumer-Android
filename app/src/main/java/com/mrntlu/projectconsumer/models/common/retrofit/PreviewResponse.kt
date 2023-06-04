package com.mrntlu.projectconsumer.models.common.retrofit

data class PreviewResponse<T>(
    val upcoming: List<T>,
    val popular: List<T>,
    val top: List<T>,
)
