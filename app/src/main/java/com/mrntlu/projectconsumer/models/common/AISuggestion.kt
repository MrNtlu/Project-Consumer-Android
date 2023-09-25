package com.mrntlu.projectconsumer.models.common

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater

data class AISuggestion(
    @SerializedName("_id")
    val id: String,

    @SerializedName("content_id")
    val contentId: String,

    @SerializedName("content_external_id")
    val contentExternalID: String?,

    @SerializedName("content_external_int_id")
    val contentExternalIntID: Int?,

    @SerializedName("content_type")
    val contentType: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("image_url")
    val imageURL: String,

    val score: Float,
    val description: String,

    @SerializedName("watch_later")
    var consumeLater: ConsumeLater?,
): DiffUtilComparison<AISuggestion>, Cloneable {
    override fun areItemsTheSame(newItem: AISuggestion): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: AISuggestion): Boolean {
        return when {
            id != newItem.id -> false
            contentId != newItem.contentId -> false
            titleOriginal != newItem.titleOriginal -> false
            consumeLater != newItem.consumeLater -> false
            else -> true
        }
    }

    override fun clone() = AISuggestion(id, contentId, contentExternalID, contentExternalIntID, contentType, titleEn, titleOriginal, imageURL, score, description, consumeLater)
}