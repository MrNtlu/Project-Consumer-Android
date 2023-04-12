package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieEntity

@Dao
interface MovieDao {

    //https://github.com/MrNtlu/PassVault/blob/master/app/src/main/java/com/mrntlu/PassVault/services/ParseDao.kt
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieList(movieList: List<MovieEntity>)

    @Query(
        "Select * From movies Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'popularity' Then tmdb_popularity End Desc," +
                "Case When :sort = 'new' Then release_date End Desc," +
                "Case When :sort = 'old' Then release_date End Asc," +
                "Case When :sort = 'later' Then release_date End Desc," +
                "Case When :sort = 'soon' Then release_date End Asc"
    )
    fun getMoviesByTag(tag: String, page: Int, sort: String): List<MovieEntity>

//    @Query(
//        "Select * From movies Where page <= :page And tag = :tag Order By " +
//                "Case When :sort = 'popularity' Then tmdb_popularity End Desc," +
//                "Case When :sort = 'new' Then release_date End Desc," +
//                "Case When :sort = 'old' Then release_date End Asc," +
//                "Case When :sort = 'later' Then release_date End Desc," +
//                "Case When :sort = 'soon' Then release_date End Asc"
//    )
//    suspend fun getAllMoviesByTag(tag: String, page: Int, sort: String): List<MovieEntity>

    @Query("Delete From movies Where tag = :tag")
    suspend fun deleteMoviesByTag(tag: String)
}