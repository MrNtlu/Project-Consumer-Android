package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrntlu.projectconsumer.models.main.movie.Actor
import com.mrntlu.projectconsumer.models.main.movie.ProductionAndCompany
import com.mrntlu.projectconsumer.models.main.movie.Streaming

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val description: String,
    val genres: List<MovieGenreEntity>,
    val streaming: List<Streaming>?,
    val actors: List<Actor>,
    val length: Int,
    val status: String,

    @ColumnInfo("image_url")
    val imageURL: String,

    @ColumnInfo("small_image_url")
    val smallImageURL: String,

    @ColumnInfo("imdb_id")
    val imdbID: String,

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

    @ColumnInfo("tmdb_vote")
    val tmdbVote: Double,

    @ColumnInfo("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @ColumnInfo("production_companies")
    val productionCompanies: List<ProductionAndCompany>,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "","", listOf(), listOf(), listOf(), 0,"","",
        "","","","","","",
        0.0, 0.0, 0, listOf(), "", 0
    )
}