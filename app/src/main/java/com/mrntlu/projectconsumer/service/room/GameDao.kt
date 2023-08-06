package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrntlu.projectconsumer.models.main.game.entity.GameEntity

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameList(movieList: List<GameEntity>)

    @Query(
        "Select * From games Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then rawg_rating End Desc," +
                "Case When :sort = 'metacritic' Then metacritic_score End Desc," +
                "Case When :sort = 'new' Then release_date End Desc," +
                "Case When :sort = 'old' Then release_date End Asc," +
                "Case When :sort = 'later' Then release_date End Desc," +
                "Case When :sort = 'soon' Then release_date End Asc"
    )
    fun getGamesByTag(tag: String, page: Int, sort: String): List<GameEntity>?

    @Query(
        "Select * From games Where page <= :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then rawg_rating End Desc," +
                "Case When :sort = 'metacritic' Then metacritic_score End Desc," +
                "Case When :sort = 'new' Then release_date End Desc," +
                "Case When :sort = 'old' Then release_date End Asc," +
                "Case When :sort = 'later' Then release_date End Desc," +
                "Case When :sort = 'soon' Then release_date End Asc"
    )
    fun getAllGamesByTag(tag: String, page: Int, sort: String): List<GameEntity>

    @Query("Select Exists(Select * From games Where page = :page And tag = :tag)")
    fun isGamePageExist(tag: String, page: Int): Boolean

    @Query("Select * From games Where page = :page And tag = :tag")
    fun getSearchGames(tag: String, page: Int): List<GameEntity>?

    @Query("Select * From games Where page <= :page And tag = :tag")
    fun getAllSearchGames(tag: String, page: Int): List<GameEntity>

    @Query("Delete From games Where tag = :tag")
    suspend fun deleteGamesByTag(tag: String)
}