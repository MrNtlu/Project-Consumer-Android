package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class UpdatePassword(
    @SerializedName("old_password")
    val oldPassword: String,

    @SerializedName("new_password")
    val newPassword: String,
)
