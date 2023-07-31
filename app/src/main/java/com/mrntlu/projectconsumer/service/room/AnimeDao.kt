package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrntlu.projectconsumer.models.main.anime.entity.AnimeEntity

@Dao
interface AnimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimeList(movieList: List<AnimeEntity>)

    //TODO: Missing GetAnimesByTag & getAllAnimesByTag

    @Query("Select Exists(Select * From animes Where page = :page And tag = :tag)")
    fun isAnimePageExist(tag: String, page: Int): Boolean

    @Query("Select * From animes Where page = :page And tag = :tag")
    fun getSearchAnimes(tag: String, page: Int): List<AnimeEntity>?

    @Query("Select * From animes Where page <= :page And tag = :tag")
    fun getAllSearchAnimes(tag: String, page: Int): List<AnimeEntity>

    @Query("Delete From animes Where tag = :tag")
    suspend fun deleteAnimesByTag(tag: String)
}