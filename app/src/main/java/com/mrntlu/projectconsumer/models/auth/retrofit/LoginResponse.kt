package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val token: String,
)