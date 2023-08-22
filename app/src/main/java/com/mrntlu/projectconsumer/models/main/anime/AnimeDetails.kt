package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DetailsModel
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userList.AnimeWatchList

data class AnimeDetails(
    @SerializedName("_id")
    val id: String,
    val description: String,
    val trailer: String?,
    val type: String,
    val source: String,
    val episodes: Int?,
    val season: String?,
    val year: Int?,
    val status: String,
    val aired: AnimeAirDate,
    val streaming: List<AnimeNameURL>?,
    val producers: List<AnimeNameURL>?,
    val studios: List<AnimeNameURL>?,
    val genres: List<AnimeGenre>?,
    val themes: List<AnimeGenre>?,
    val demographics: List<AnimeGenre>?,
    val relations: List<AnimeRelation>?,
    val characters: List<AnimeCharacter>?,

    @SerializedName("title_original")
    val titleOriginal: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("title_jp")
    val titleJp: String,

    @SerializedName("image_url")
    val imageURL: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("mal_score")
    val malScore: Float,

    @SerializedName("mal_scored_by")
    val malScoredBy: Int,

    @SerializedName("is_airing")
    val isAiring: Boolean,

    @SerializedName("age_rating")
    val ageRating: String?,

    @SerializedName("anime_list")
    override var watchList: AnimeWatchList?,

    @SerializedName("watch_later")
    override var consumeLater: ConsumeLater?,
): DetailsModel<AnimeWatchList>()
