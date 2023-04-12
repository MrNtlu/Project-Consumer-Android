package com.mrntlu.projectconsumer.models.main.movie.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.MovieGenre
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieEntity
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieGenreEntity

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
                movie.genres.map { MovieGenreEntity(it.name, it.tmdbID) },
                movie.streamingList,
                movie.length,
                movie.status,
                movie.imageURL,
                movie.smallImageURL,
                movie.imdbID,
                movie.releaseDate,
                movie.titleEn,
                movie.titleOriginal,
                movie.tmdbID,
                movie.tmdbPopularity,
                movie.tmdbVote,
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
                movieEntity.genres.map { MovieGenre(it.name, it.tmdbID) },
                movieEntity.streamingList,
                movieEntity.length,
                movieEntity.status,
                movieEntity.imageURL,
                movieEntity.smallImageURL,
                movieEntity.imdbID,
                movieEntity.releaseDate,
                movieEntity.titleEn,
                movieEntity.titleOriginal,
                movieEntity.tmdbID,
                movieEntity.tmdbPopularity,
                movieEntity.tmdbVote,
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