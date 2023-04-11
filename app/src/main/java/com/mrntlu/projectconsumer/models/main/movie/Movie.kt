package com.mrntlu.projectconsumer.models.main.movie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movies")
data class Movie(
    @SerializedName("_id")
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val description: String,
    val genres: List<MovieGenre>,
    val streaming: List<Streaming>,
    val length: Int,
    val status: String,

    @SerializedName("image_url")
    @ColumnInfo("image_url")
    val imageURL: String,

    @SerializedName("small_image_url")
    @ColumnInfo("small_image_url")
    val smallImageURL: String,

    @SerializedName("imdb_id")
    @ColumnInfo("imdb_id")
    val imdbID: String,

    @SerializedName("release_date")
    @ColumnInfo("release_date")
    val releaseDate: String,

    @SerializedName("title_en")
    @ColumnInfo("title_en")
    val titleEn: String,

    @SerializedName("title_original")
    @ColumnInfo("title_original")
    val titleOriginal: String,

    @SerializedName("tmdb_id")
    @ColumnInfo("tmdb_id")
    val tmdbID: String,

    @SerializedName("tmdb_popularity")
    @ColumnInfo("tmdb_popularity")
    val tmdbPopularity: Double,

    @SerializedName("tmdb_vote")
    @ColumnInfo("tmdb_vote")
    val tmdbVote: Double,

    @SerializedName("tmdb_vote_count")
    @ColumnInfo("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @SerializedName("production_companies")
    @ColumnInfo("production_companies")
    val productionCompanies: List<ProductionAndCompany>,
)