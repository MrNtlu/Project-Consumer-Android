package com.mrntlu.projectconsumer.models.main.game

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DetailsModel
import com.mrntlu.projectconsumer.models.common.ReviewSummary
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userList.GamePlayList

data class GameDetails(
    @SerializedName("_id")
    val id: String,
    val description: String,
    val tba: Boolean,
    val subreddit: String?,
    val genres: List<String>,
    val tags: List<String>,
    val platforms: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val stores: List<GameStore>,
    val screenshots: List<String>,
    val reviews: ReviewSummary,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("rawg_id")
    val rawgID: Int,

    @SerializedName("rawg_rating")
    val rawgRating: Float,

    @SerializedName("rawg_rating_count")
    val rawgRatingCount: Int,

    @SerializedName("metacritic_score")
    val metacriticScore: Int?,

    @SerializedName("metacritic_score_by_platform")
    val metacriticScoreByPlatform: List<GameMetacriticScorePlatform>,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("age_rating")
    val ageRating: String?,

    @SerializedName("related_games")
    val relatedGames: List<GameDetailsRelation>,

    @SerializedName("game_list")
    override var watchList: GamePlayList?,

    @SerializedName("watch_later")
    override var consumeLater: ConsumeLater?,
): DetailsModel<GamePlayList>()
