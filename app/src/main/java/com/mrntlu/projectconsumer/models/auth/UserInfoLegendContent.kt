package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel

data class UserInfoCommon(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("times_finished")
    val timesFinished: Int,
): ContentModel()

data class UserInfoGame(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("title")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("times_finished")
    val timesFinished: Int,
): ContentModel()
