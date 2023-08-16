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

    @Query(
        "Select * From consume_laters Where page = :page And tag = :tag Order By " +
                "Case When :sort = 'new' Then created_at End Desc," +
                "Case When :sort = 'old' Then created_at End Asc," +
                "Case When :sort = 'alphabetical' Then title_original End Asc," +
                "Case When :sort = 'unalphabetical' Then title_original End Desc"
    )
    fun getConsumeLatersByTag(tag: String, page: Int, sort: String): List<ConsumeLaterEntity>?

    @Query(
        "Select * From consume_laters Where page <= :page And tag = :tag Order By " +
                "Case When :sort = 'new' Then created_at End Desc," +
                "Case When :sort = 'old' Then created_at End Asc," +
                "Case When :sort = 'alphabetical' Then title_original End Asc," +
                "Case When :sort = 'unalphabetical' Then title_original End Desc"
    )
    fun getAllConsumeLatersByTag(tag: String, page: Int, sort: String): List<ConsumeLaterEntity>

    @Query("Delete From consume_laters Where tag = :tag")
    suspend fun deleteConsumeLatersByTag(tag: String)
}