package com.mrntlu.projectconsumer.models.auth.retrofit

import com.google.gson.annotations.SerializedName

data class UpdateMembershipBody(
    @SerializedName("is_premium")
    val isPremium: Boolean,

    @SerializedName("membership_type")
    val membershipType: Int
)
