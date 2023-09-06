package com.mrntlu.projectconsumer.models.main.anime

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.ContentModel

data class Anime(
    @SerializedName("_id")
    override val id: String,
    override val description: String,
    val trailer: String?,
    val type: String,
    val source: String,
    override val episodes: Int?,
    val season: String?,
    val year: Int?,
    val status: String,
    override val aired: AnimeAirDate,
    val streaming: List<AnimeNameURL>?,
    val producers: List<AnimeNameURL>?,
    val studios: List<AnimeNameURL>?,
    val genres: List<AnimeGenre>?,
    val themes: List<AnimeGenre>?,
    val demographics: List<AnimeGenre>?,
    val relations: List<AnimeRelation>?,
    val characters: List<AnimeCharacter>?,

    @SerializedName("title_en")
    override val title: String,

    @SerializedName("title_original")
    override val titleOriginal: String,

    @SerializedName("title_jp")
    val titleJP: String,

    @SerializedName("image_url")
    override val imageURL: String,

    @SerializedName("mal_id")
    val malID: Int,

    @SerializedName("mal_score")
    override val score: Float,

    @SerializedName("mal_scored_by")
    val malScoredBy: Int,

    @SerializedName("is_airing")
    val isAiring: Boolean,

    @SerializedName("age_rating")
    val ageRating: String?,

    override val length: Int? = null,
    override val releaseDate: String? = null,
    override val totalSeasons: Int? = null,
): ContentModel()
