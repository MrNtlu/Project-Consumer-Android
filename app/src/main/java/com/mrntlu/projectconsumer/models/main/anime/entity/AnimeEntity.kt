package com.mrntlu.projectconsumer.models.main.anime.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate
import com.mrntlu.projectconsumer.models.main.anime.AnimeCharacter
import com.mrntlu.projectconsumer.models.main.anime.AnimeGenre
import com.mrntlu.projectconsumer.models.main.anime.AnimeNameURL
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation

@Entity(tableName = "animes")
data class AnimeEntity(
    @PrimaryKey(autoGenerate = false)
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

    @ColumnInfo("title_en")
    val title: String,

    @ColumnInfo("title_original")
    val titleOriginal: String,

    @ColumnInfo("title_jp")
    val titleJP: String,

    @ColumnInfo("image_url")
    val imageURL: String,

    @ColumnInfo("mal_id")
    val malID: Int,

    @ColumnInfo("mal_score")
    val malScore: Float,

    @ColumnInfo("mal_scored_by")
    val malScoredBy: Int,

    @ColumnInfo("is_airing")
    val isAiring: Boolean,

    @ColumnInfo("age_rating")
    val ageRating: String?,

    val tag: String,
    val page: Int,
) {
    constructor(): this(
        "", "", null, "", "", null, null, null, "",
        AnimeAirDate(), listOf(), listOf(), listOf(), listOf(), listOf(), listOf(), listOf(), listOf(), "", "",
        "", "", -1, 0.0f, 0, false, null, "", 0
    )
}