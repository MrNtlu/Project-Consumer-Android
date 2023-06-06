package com.mrntlu.projectconsumer.models.common.retrofit

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    val code: Int,
    val message: String
)

data class ErrorAltResponse(
    @SerializedName("error")
    val error: String
)
