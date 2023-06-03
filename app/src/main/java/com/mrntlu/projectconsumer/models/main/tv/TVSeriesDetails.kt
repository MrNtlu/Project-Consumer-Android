package com.mrntlu.projectconsumer.models.main.tv

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DetailsModel
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.TmdbGenre
import com.mrntlu.projectconsumer.models.common.Translation
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList

data class TVSeriesDetails(
    @SerializedName("_id")
    val id: String,
    val description: String,
    val actors: List<Actor>?,
    val genres: List<TmdbGenre>,
    val networks: List<Network>?,
    val seasons: List<Season>,
    val translations: List<Translation>?,
    val status: String,
    val streaming: List<Streaming>?,
    val backdrop: String?,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("small_image_url")
    val smallImageURL: String,

    @SerializedName("first_air_date")
    val firstAirDate: String,

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

    @SerializedName("total_episodes")
    val totalEpisodes: Int,

    @SerializedName("total_seasons")
    val totalSeasons: Int,

    @SerializedName("production_companies")
    val productionCompanies: List<ProductionAndCompany>,

    @SerializedName("tv_list")
    override var watchList: TVSeriesWatchList?,

    @SerializedName("watch_later")
    override var consumeLater: ConsumeLater?,
): DetailsModel<TVSeriesWatchList>()
