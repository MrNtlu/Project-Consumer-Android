package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.ReviewSummary
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.entity.TranslationEntity

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val description: String,
    val genres: List<String>,
    val streaming: List<Streaming>?,
    val actors: List<Actor>?,
    val translations: List<TranslationEntity>?,
    val length: Int,
    val status: String,
    val backdrop: String?,
    val reviews: ReviewSummary,

    @ColumnInfo("image_url")
    val imageURL: String,

    @ColumnInfo("imdb_id")
    val imdbID: String?,

    @ColumnInfo("release_date")
    val releaseDate: String,

    @ColumnInfo("title_en")
    val titleEn: String,

    @ColumnInfo("title_original")
    val titleOriginal: String,

    @ColumnInfo("tmdb_id")
    val tmdbID: String,

    @ColumnInfo("tmdb_popularity")
    val tmdbPopularity: Double,

    @ColumnInfo("top_rated")
    val topRated: Double,

    @ColumnInfo("tmdb_vote")
    val tmdbVote: Double,

    @ColumnInfo("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @ColumnInfo("production_companies")
    val productionCompanies: List<ProductionAndCompany>?,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "","", listOf(), listOf(), listOf(), listOf(), 0,"", null, ReviewSummary(),
        "",  "","","","","", 0.0,
        0.0, 0.0, 0, listOf(), "", 0
    )
}