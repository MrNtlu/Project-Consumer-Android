package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class ForgotPasswordBody(
    @SerializedName("email_address")
    private val email: String
)
