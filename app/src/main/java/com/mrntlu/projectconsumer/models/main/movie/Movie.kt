package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("_id")
    val id: String,
    val description: String,
    val genres: List<MovieGenre>,
    val streamingList: List<Streaming>?,
    val length: Int,
    val status: String,
    @SerializedName("image_url")
    val imageURL: String,
    @SerializedName("small_image_url")
    val smallImageURL: String,
    @SerializedName("imdb_id")
    val imdbID: String,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("title_en")
    val titleEn: String,
    @SerializedName("title_original")
    val titleOriginal: String,
    @SerializedName("tmdb_id")
    val tmdbID: String,
    @SerializedName("tmdb_popularity")
    val tmdbPopularity: Double,
    @SerializedName("tmdb_vote")
    val tmdbVote: Double,
    @SerializedName("tmdb_vote_count")
    val tmdbVoteCount: Int,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionAndCompany>,
)