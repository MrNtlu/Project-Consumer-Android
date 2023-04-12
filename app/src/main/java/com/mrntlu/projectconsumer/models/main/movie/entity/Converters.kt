package com.mrntlu.projectconsumer.models.main.movie.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.main.movie.ProductionAndCompany
import com.mrntlu.projectconsumer.models.main.movie.Streaming

class Converters {

    @TypeConverter
    fun fromGenreEntity(genres: List<MovieGenreEntity>?): String {
        return Gson().toJson(genres)
    }

    @TypeConverter
    fun toGenreEntity(genres: String?): List<MovieGenreEntity>? {
        if (genres != null) {
            val genre = object : TypeToken<List<MovieGenreEntity>>() {}.type
            return Gson().fromJson(genres, genre)
        }
        return emptyList()
    }

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

    @TypeConverter
    fun fromProductionAndCompanyList(productionCompanies: List<ProductionAndCompany>?): String {
        return Gson().toJson(productionCompanies)
    }

    @TypeConverter
    fun toProductionAndCompanyList(productionCompanies: String?): List<ProductionAndCompany>? {
        if (productionCompanies != null) {
            val productionAndCompany = object : TypeToken<List<ProductionAndCompany>>() {}.type
            return Gson().fromJson(productionCompanies, productionAndCompany)
        }
        return emptyList()
    }
}