package com.mrntlu.projectconsumer.models.main.game

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel

data class Game(
    @SerializedName("_id")
    override val id: String,
    val description: String,
    val tba: Boolean,
    val subreddit: String?,
    val genres: List<GameGenre>,
    val tags: List<String>,
    val platforms: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val stores: List<GameStore>,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("rawg_id")
    val rawgID: Int,

    @SerializedName("rawg_rating")
    val rawgRating: Float,

    @SerializedName("rawg_rating_count")
    val rawgRatingCount: Int,

    @SerializedName("metacritic_score")
    val metacriticScore: Int,

    @SerializedName("metacritic_score_by_platform")
    val metacriticScoreByPlatform: List<GameMetacriticScorePlatform>,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("background_image")
    override val imageURL: String,

    @SerializedName("age_rating")
    val ageRating: String?,

    @SerializedName("related_games")
    val relatedGames: List<GameRelation>,

    @SerializedName("has_release_date")
    val hasReleaseDate: Boolean,
): ContentModel()
