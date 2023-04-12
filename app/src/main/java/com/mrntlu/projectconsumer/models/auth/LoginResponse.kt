package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val token: String
)