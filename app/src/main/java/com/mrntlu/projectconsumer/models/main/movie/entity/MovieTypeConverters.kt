package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.main.movie.Streaming

class MovieTypeConverters {
    @TypeConverter
    fun fromStreamingList(streamingList: List<Streaming>?): String {
        return Gson().toJson(streamingList)
    }

    @TypeConverter
    fun toStreamingList(streamingList: String?): List<Streaming>? {
        if (streamingList != null) {
            val streaming = object : TypeToken<List<Streaming>>() {}.type
            return Gson().fromJson(streamingList, streaming)
        }
        return emptyList()
    }
}