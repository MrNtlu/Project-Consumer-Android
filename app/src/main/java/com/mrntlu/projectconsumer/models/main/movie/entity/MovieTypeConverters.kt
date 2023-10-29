package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.common.Streaming

class MovieTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStreamingList(streamingList: List<Streaming>?): String {
        return gson.toJson(streamingList)
    }

    @TypeConverter
    fun toStreamingList(streamingList: String?): List<Streaming>? {
        if (streamingList != null) {
            val streaming = object : TypeToken<List<Streaming>>() {}.type
            return gson.fromJson(streamingList, streaming)
        }
        return emptyList()
    }
}