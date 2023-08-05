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

    @Query(
        "Select * From animes Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then mal_score End Desc," +
                "Case When :sort = 'new' Then \"aired.from\" End Desc," +
                "Case When :sort = 'old' Then \"aired.from\" End Asc," +
                "Case When :sort = 'later' Then \"aired.from\" End Desc," +
                "Case When :sort = 'soon' Then \"aired.from\" End Asc"
    )
    fun getAnimesByTag(tag: String, page: Int, sort: String): List<AnimeEntity>?

    @Query(
        "Select * From animes Where page <= :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then mal_score End Desc," +
                "Case When :sort = 'new' Then \"aired.from\" End Desc," +
                "Case When :sort = 'old' Then \"aired.from\" End Asc," +
                "Case When :sort = 'later' Then \"aired.from\" End Desc," +
                "Case When :sort = 'soon' Then \"aired.from\" End Asc"
    )
    fun getAllAnimesByTag(tag: String, page: Int, sort: String): List<AnimeEntity>

    @Query("Select Exists(Select * From animes Where page = :page And tag = :tag)")
    fun isAnimePageExist(tag: String, page: Int): Boolean

    @Query("Select * From animes Where page = :page And tag = :tag")
    fun getSearchAnimes(tag: String, page: Int): List<AnimeEntity>?

    @Query("Select * From animes Where page <= :page And tag = :tag")
    fun getAllSearchAnimes(tag: String, page: Int): List<AnimeEntity>

    @Query("Delete From animes Where tag = :tag")
    suspend fun deleteAnimesByTag(tag: String)
}