package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListContentModel

data class GameList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("content_status")
    override val contentStatus: String,

    override val score: Int?,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("hours_played")
    override val mainAttribute: Int?,

    @SerializedName("game_id")
    override val contentId: String,

    @SerializedName("rawg_id")
    override val contentExternalId: String,

    @SerializedName("title")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("image_url")
    override val imageUrl: String?,

    @SerializedName("created_at")
    override val createdAt: String,

    override val totalSeasons: Int? = null,
    override val subAttribute: Int? = null,
    override val totalEpisodes: Int? = null,
): UserListContentModel()

fun UserListContentModel.convertToGameList(): GameList {
    return this.run {
        GameList(
            id, contentStatus, score, timesFinished, mainAttribute,
            contentId, contentExternalId, title, titleOriginal,
            imageUrl, createdAt,
        )
    }
}
