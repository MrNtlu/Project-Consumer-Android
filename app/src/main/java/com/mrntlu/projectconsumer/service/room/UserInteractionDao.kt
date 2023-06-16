package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrntlu.projectconsumer.models.main.userInteraction.entity.ConsumeLaterEntity

@Dao
interface UserInteractionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumeLaterList(consumeLaterList: List<ConsumeLaterEntity>)

    //TODO Test, add filter by content type and local search

    @Query(
        "Select * From consume_laters Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'new' Then created_at End Desc," +
                "Case When :sort = 'old' Then created_at End Asc," +
                "Case When :sort = 'alphabetical' Then content_title_original End Desc," +
                "Case When :sort = 'unalphabetical' Then content_title_original End Asc"
    )
    fun getConsumeLatersByTag(tag: String, page: Int, sort: String): List<ConsumeLaterEntity>?

    @Query(
        "Select * From consume_laters Where page <= :page And tag = :tag Order By " +
                "Case When :sort = 'new' Then created_at End Desc," +
                "Case When :sort = 'old' Then created_at End Asc," +
                "Case When :sort = 'alphabetical' Then content_title_original End Desc," +
                "Case When :sort = 'unalphabetical' Then content_title_original End Asc"
    )
    fun getAllConsumeLatersByTag(tag: String, page: Int, sort: String): List<ConsumeLaterEntity>

    @Query("Delete From consume_laters Where tag = :tag")
    suspend fun deleteConsumeLatersByTag(tag: String)
}