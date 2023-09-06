package com.mrntlu.projectconsumer.models.main.tv

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.Translation
import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate

data class TVSeries(
    @SerializedName("_id")
    override val id: String,
    override val description: String,
    val actors: List<Actor>?,
    val genres: List<String>,
    val networks: List<Network>?,
    val seasons: List<Season>,
    val translations: List<Translation>?,
    val status: String,
    val streaming: List<Streaming>?,
    val backdrop: String?,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("first_air_date")
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

    @SerializedName("total_episodes")
    override val episodes: Int,

    @SerializedName("total_seasons")
    override val totalSeasons: Int,

    @SerializedName("production_companies")
    val productionCompanies: List<ProductionAndCompany>?,

    override val length: Int? = null,
    override val aired: AnimeAirDate? = null,
): ContentModel()