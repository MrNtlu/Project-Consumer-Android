package com.mrntlu.projectconsumer.models.main.game.mapper

import com.mrntlu.projectconsumer.interfaces.EntityMapper
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.models.main.game.entity.GameEntity

object GameEntityMapper: EntityMapper<List<Game>, List<GameEntity>> {
    override fun asEntity(model: List<Game>, tag: String, page: Int): List<GameEntity> {
        return model.map { game ->
            game.run {
                GameEntity(
                    id, description, tba, popularity, subreddit, genres, tags, platforms, developers, publishers, stores,
                    title, titleOriginal, rawgID, rawgRating, rawgRatingCount, metacriticScore, metacriticScoreByPlatform,
                    releaseDate, imageURL, ageRating, relatedGames, hasReleaseDate, tag, page,
                )
            }
        }
    }

    override fun asModel(entity: List<GameEntity>): List<Game> {
        return entity.map { gameEntity ->
            gameEntity.run {
                Game(
                    id, description, tba, popularity, subreddit, genres, tags, platforms, developers, publishers, stores,
                    title, titleOriginal, rawgID, rawgRating, rawgRatingCount, metacriticScore, metacriticScoreByPlatform,
                    releaseDate, imageURL, ageRating, relatedGames, hasReleaseDate,
                )
            }
        }
    }
}

fun List<Game>.asEntity(tag: String, page: Int): List<GameEntity> {
    return GameEntityMapper.asEntity(this, tag, page)
}

fun List<GameEntity>.asModel(): List<Game> {
    return GameEntityMapper.asModel(this)
}