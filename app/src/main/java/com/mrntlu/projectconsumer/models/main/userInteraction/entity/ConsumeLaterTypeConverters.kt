package com.mrntlu.projectconsumer.models.main.userInteraction.entity

import androidx.room.TypeConverter
import com.google.gson.Gson

class ConsumeLaterTypeConverters {
    @TypeConverter
    fun fromConsumeLaterContent(consumeLaterContentEntity: ConsumeLaterContentEntity): String {
        return Gson().toJson(consumeLaterContentEntity)
    }

    @TypeConverter
    fun toConsumeLaterContent(consumeLaterContent: String): ConsumeLaterContentEntity {
        return Gson().fromJson(consumeLaterContent, ConsumeLaterContentEntity::class.java)
    }
}