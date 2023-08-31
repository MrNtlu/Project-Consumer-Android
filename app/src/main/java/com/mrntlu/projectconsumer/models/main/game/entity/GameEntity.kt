package com.mrntlu.projectconsumer.models.main.game.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrntlu.projectconsumer.models.main.game.GameMetacriticScorePlatform
import com.mrntlu.projectconsumer.models.main.game.GameRelation
import com.mrntlu.projectconsumer.models.main.game.GameStore

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val description: String,
    val tba: Boolean,
    val popularity: Float,
    val subreddit: String?,
    val genres: List<String>,
    val tags: List<String>,
    val platforms: List<String>,
    val developers: List<String>,
    val publishers: List<String>,
    val stores: List<GameStore>?,

    @ColumnInfo("title")
    val title: String,

    @ColumnInfo("title_original")
    val titleOriginal: String,

    @ColumnInfo("rawg_id")
    val rawgID: Int,

    @ColumnInfo("rawg_rating")
    val rawgRating: Float,

    @ColumnInfo("rawg_rating_count")
    val rawgRatingCount: Int,

    @ColumnInfo("metacritic_score")
    val metacriticScore: Int,

    @ColumnInfo("metacritic_score_by_platform")
    val metacriticScoreByPlatform: List<GameMetacriticScorePlatform>,

    @ColumnInfo("release_date")
    val releaseDate: String,

    @ColumnInfo("image_url")
    val imageURL: String,

    @ColumnInfo("age_rating")
    val ageRating: String?,

    @ColumnInfo("related_games")
    val relatedGames: List<GameRelation>,

    @ColumnInfo("has_release_date")
    val hasReleaseDate: Boolean,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "", "", false, 0.0f, null, listOf(), listOf(), listOf(), listOf(), listOf(), listOf(),
        "", "" , 0, 0.0f, 0, 0, listOf(), "",
        "", null, listOf(), false, "", 0,
    )
}
