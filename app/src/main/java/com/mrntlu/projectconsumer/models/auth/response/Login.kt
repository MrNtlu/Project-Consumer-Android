package com.mrntlu.projectconsumer.models.auth.response

import com.google.gson.annotations.SerializedName

data class Login(
    @SerializedName("access_token")
    val token: String
)