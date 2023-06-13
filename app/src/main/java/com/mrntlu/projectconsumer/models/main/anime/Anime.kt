package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel

data class Anime(
    @SerializedName("_id")
    override val id: String,
    override val description: String,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("small_image_url")
    val smallImageURL: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,
): ContentModel()
