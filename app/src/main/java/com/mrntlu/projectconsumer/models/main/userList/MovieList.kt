package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.UserListContentModel

data class MovieList(
    @SerializedName("_id")
    override val id: String,

    @SerializedName("content_status")
    override val contentStatus: String,

    override val score: Int?,

    @SerializedName("times_finished")
    override val timesFinished: Int,

    @SerializedName("movie_id")
    override val contentId: String,

    @SerializedName("tmdb_id")
    override val contentExternalId: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("image_url")
    override val imageUrl: String?,

    override val mainAttribute: Int? = null,
    override val totalSeasons: Int = 1,
    override val totalEpisodes: Int? = null,
    override val subAttribute: Int? = null,
): UserListContentModel()

fun UserListContentModel.convertToMovieList(): MovieList {
    return this.run {
        MovieList(
            id, contentStatus, score, timesFinished,
            contentId, contentExternalId,
            title, titleOriginal, imageUrl,
        )
    }
}
