package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrntlu.projectconsumer.models.main.tv.entity.TVSeriesEntity

@Dao
interface TVSeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTVSeriesList(tvList: List<TVSeriesEntity>)

    @Query("Select Exists(Select * From `tv-series` Where page = :page And tag = :tag)")
    fun isTVSeriesPageExist(tag: String, page: Int): Boolean

    @Query(
        "Select * From `tv-series` Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then tmdb_popularity End Desc," +
                "Case When :sort = 'new' Then first_air_date End Desc," +
                "Case When :sort = 'old' Then first_air_date End Asc," +
                "Case When :sort = 'later' Then first_air_date End Desc," +
                "Case When :sort = 'soon' Then first_air_date End Asc"
    )
    fun getTVSeriesByTag(tag: String, page: Int, sort: String): List<TVSeriesEntity>?

    @Query(
        "Select * From `tv-series` Where page <= :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then tmdb_popularity End Desc," +
                "Case When :sort = 'new' Then first_air_date End Desc," +
                "Case When :sort = 'old' Then first_air_date End Asc," +
                "Case When :sort = 'later' Then first_air_date End Desc," +
                "Case When :sort = 'soon' Then first_air_date End Asc"
    )
    fun getAllTVSeriesByTag(tag: String, page: Int, sort: String): List<TVSeriesEntity>

    @Query("Select * From `tv-series` Where page = :page And tag = :tag")
    fun getSearchTVSeries(tag: String, page: Int): List<TVSeriesEntity>?

    @Query("Select * From `tv-series` Where page <= :page And tag = :tag")
    fun getAllSearchTVSeries(tag: String, page: Int): List<TVSeriesEntity>

    @Query("Delete From `tv-series` Where tag = :tag")
    suspend fun deleteTVSeriesByTag(tag: String)
}