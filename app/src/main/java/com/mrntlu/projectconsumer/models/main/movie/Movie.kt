package com.mrntlu.projectconsumer.models.main.movie

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.ReviewSummary
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.Translation
import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate

data class Movie(
    @SerializedName("_id")
    override val id: String,
    override val description: String,
    val genres: List<String>,
    val streaming: List<Streaming>?,
    val actors: List<Actor>?,
    val translations: List<Translation>?,
    override val length: Int,
    val status: String,
    val backdrop: String?,
    val reviews: ReviewSummary,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("imdb_id")
    val imdbID: String?,

    @SerializedName("release_date")
    override val releaseDate: String,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("tmdb_id")
    val tmdbID: String,

    @SerializedName("tmdb_popularity")
    val tmdbPopularity: Double,

    @SerializedName("top_rated")
    val topRated: Double,

    @SerializedName("tmdb_vote")
    override val score: Float,

    @SerializedName("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @SerializedName("production_companies")
    val productionCompanies: List<ProductionAndCompany>?,

    override val episodes: Int? = null,
    override val totalSeasons: Int? = null,
    override val aired: AnimeAirDate? = null,
): ContentModel()