package com.mrntlu.projectconsumer.models.main.tv

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.Translation

data class TVSeries(
    @SerializedName("_id")
    override val id: String,
    val description: String,
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
    val firstAirDate: String,

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
    val tmdbVote: Double,

    @SerializedName("tmdb_vote_count")
    val tmdbVoteCount: Int,

    @SerializedName("total_episodes")
    val totalEpisodes: Int,

    @SerializedName("total_seasons")
    val totalSeasons: Int,

    @SerializedName("production_companies")
    val productionCompanies: List<ProductionAndCompany>?,
): ContentModel()