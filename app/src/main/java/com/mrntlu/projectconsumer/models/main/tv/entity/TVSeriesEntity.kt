package com.mrntlu.projectconsumer.models.main.tv.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.entity.TmdbGenreEntity
import com.mrntlu.projectconsumer.models.common.entity.TranslationEntity
import com.mrntlu.projectconsumer.models.main.tv.Season

@Entity(tableName = "tv-series")
data class TVSeriesEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val description: String,
    val actors: List<Actor>?,
    val genres: List<TmdbGenreEntity>,
    val networks: List<NetworkEntity>?,
    val seasons: List<Season>,
    val translations: List<TranslationEntity>?,
    val status: String,
    val streaming: List<Streaming>?,
    val backdrop: String?,

    @ColumnInfo("image_url")
    val imageURL: String,

    @ColumnInfo("small_image_url")
    val smallImageURL: String,

    @ColumnInfo("first_air_date")
    val firstAirDate: String,

    @ColumnInfo("title_en")
    val title: String,

    @ColumnInfo("title_original")
    val titleOriginal: String,

    @ColumnInfo("tmdb_id")
    val tmdbID: String,

    @ColumnInfo("tmdb_popularity")
    val tmdbPopularity: Double,

    @ColumnInfo("tmdb_vote")
    val tmdbVote: Double,

    @ColumnInfo("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @ColumnInfo("total_episodes")
    val totalEpisodes: Int,

    @ColumnInfo("total_seasons")
    val totalSeasons: Int,

    @ColumnInfo("production_companies")
    val productionCompanies: List<ProductionAndCompany>?,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "", "", listOf(), listOf(), listOf(), listOf(), listOf(), "", listOf(), "",
        "", "", "", "", "", "", 0.0, 0.0,
        0, 0, 0, listOf(), "", 0
    )
}
