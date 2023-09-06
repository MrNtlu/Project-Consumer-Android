package com.mrntlu.projectconsumer.models.main.movie.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.common.Translation
import com.mrntlu.projectconsumer.models.common.entity.TranslationEntity
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieEntity

object MovieEntityMapper: EntityMapper<List<Movie>, List<MovieEntity>> {
    override fun asEntity(
        model: List<Movie>,
        tag: String,
        page: Int,
    ): List<MovieEntity> {
        return model.map { movie ->
            MovieEntity(
                movie.id,
                movie.description,
                movie.genres,
                movie.streaming,
                movie.actors,
                movie.translations?.map { TranslationEntity(it.lanCode, it.lanName, it.lanNameEn, it.title, it.description) },
                movie.length,
                movie.status,
                movie.backdrop,
                movie.imageURL,
                movie.imdbID,
                movie.releaseDate,
                movie.title,
                movie.titleOriginal,
                movie.tmdbID,
                movie.tmdbPopularity,
                movie.topRated,
                movie.score.toDouble(),
                movie.tmdbVoteCount,
                movie.productionCompanies,
                tag,
                page,
            )
        }
    }

    override fun asModel(entity: List<MovieEntity>): List<Movie> {
        return entity.map { movieEntity ->
            Movie(
                movieEntity.id,
                movieEntity.description,
                movieEntity.genres,
                movieEntity.streaming,
                movieEntity.actors,
                movieEntity.translations?.map { Translation(it.lanCode, it.lanName, it.lanNameEn, it.title, it.description) },
                movieEntity.length,
                movieEntity.status,
                movieEntity.backdrop,
                movieEntity.imageURL,
                movieEntity.imdbID,
                movieEntity.releaseDate,
                movieEntity.titleEn,
                movieEntity.titleOriginal,
                movieEntity.tmdbID,
                movieEntity.tmdbPopularity,
                movieEntity.topRated,
                movieEntity.tmdbVote.toFloat(),
                movieEntity.tmdbVoteCount,
                movieEntity.productionCompanies,
            )
        }
    }
}

fun List<Movie>.asEntity(tag: String, page: Int): List<MovieEntity> {
    return MovieEntityMapper.asEntity(this, tag, page)
}

fun List<MovieEntity>.asModel(): List<Movie> {
    return MovieEntityMapper.asModel(this)
}