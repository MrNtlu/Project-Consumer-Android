package com.mrntlu.projectconsumer.models.main.anime.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.anime.entity.AnimeEntity

object AnimeEntityMapper: EntityMapper<List<Anime>, List<AnimeEntity>> {
    override fun asEntity(model: List<Anime>, tag: String, page: Int): List<AnimeEntity> {
        return model.map { anime ->
            anime.run {
                AnimeEntity(
                    id, description, trailer, type, source, episodes, season, year,
                    status, aired, streaming, producers, studios, genres, themes, demographics,
                    relations, characters, title, titleOriginal, titleJP, imageURL,
                    malID, score, malScoredBy, isAiring, ageRating, tag, page
                )
            }
        }
    }

    override fun asModel(entity: List<AnimeEntity>): List<Anime> {
        return entity.map { animeEntity ->
            animeEntity.run {
                Anime(
                    id, description, trailer, type, source, episodes, season, year,
                    status, aired, streaming, producers, studios, genres, themes, demographics,
                    relations, characters, title, titleOriginal, titleJP, imageURL,
                    malID, malScore, malScoredBy, isAiring, ageRating,
                )
            }
        }
    }
}

fun List<Anime>.asEntity(tag: String, page: Int): List<AnimeEntity> {
    return AnimeEntityMapper.asEntity(this, tag, page)
}

fun List<AnimeEntity>.asModel(): List<Anime> {
    return AnimeEntityMapper.asModel(this)
}