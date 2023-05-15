package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList

data class MovieDetails(
    @SerializedName("_id")
    val id: String,
    val description: String,
    val genres: List<MovieGenre>,
    val streaming: List<Streaming>?,
    val actors: List<Actor>?,
    val translations: List<Translation>?,
    val length: Int,
    val status: String,
    val backdrop: String?,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("small_image_url")
    val smallImageURL: String,

    @SerializedName("imdb_id")
    val imdbID: String?,

    @SerializedName("release_date")
    val releaseDate: String,

    @SerializedName("title_en")
    val title: String,

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
    val productionCompanies: List<ProductionAndCompany>?,

    @SerializedName("watch_list")
    val movieWatchList: MovieWatchList?,

    @SerializedName("watch_later")
    val watchLater: ConsumeLater?,
)
