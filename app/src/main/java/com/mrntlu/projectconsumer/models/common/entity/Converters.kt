package com.mrntlu.projectconsumer.models.common.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.common.Actor
import com.mrntlu.projectconsumer.models.common.ProductionAndCompany

class Converters {

    @TypeConverter
    fun fromGenreEntity(genres: List<TmdbGenreEntity>?): String {
        return Gson().toJson(genres)
    }

    @TypeConverter
    fun toGenreEntity(genres: String?): List<TmdbGenreEntity>? {
        if (genres != null) {
            val genre = object : TypeToken<List<TmdbGenreEntity>>() {}.type
            return Gson().fromJson(genres, genre)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromActorList(actors: List<Actor>?): String {
        return Gson().toJson(actors)
    }

    @TypeConverter
    fun toActorList(actors: String?): List<Actor>? {
        if (actors != null) {
            val actor = object : TypeToken<List<Actor>>() {}.type
            return Gson().fromJson(actors, actor)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromTranslationList(translations: List<TranslationEntity>?): String {
        return Gson().toJson(translations)
    }

    @TypeConverter
    fun toTranslationList(translations: String?): List<TranslationEntity>? {
        if (translations != null) {
            val translation = object : TypeToken<List<TranslationEntity>>() {}.type
            return Gson().fromJson(translations, translation)
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