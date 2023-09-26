package com.mrntlu.projectconsumer.models.common

import com.google.gson.annotations.SerializedName

data class AISuggestionResponse(
    val suggestions: List<AISuggestion>,

    @SerializedName("created_at")
    val createdAt: String,
)
