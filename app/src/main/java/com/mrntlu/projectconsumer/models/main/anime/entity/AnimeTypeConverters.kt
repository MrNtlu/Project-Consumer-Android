package com.mrntlu.projectconsumer.models.main.anime.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate
import com.mrntlu.projectconsumer.models.main.anime.AnimeCharacter
import com.mrntlu.projectconsumer.models.main.anime.AnimeGenre
import com.mrntlu.projectconsumer.models.main.anime.AnimeNameURL
import com.mrntlu.projectconsumer.models.main.anime.AnimeRelation

class AnimeTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromAirDate(airDate: AnimeAirDate): String {
        return gson.toJson(airDate)
    }

    @TypeConverter
    fun toAirDate(airDate: String): AnimeAirDate {
        val animeAirDate = object : TypeToken<AnimeAirDate>() {}.type
        return gson.fromJson(airDate, animeAirDate)
    }

    @TypeConverter
    fun fromNameUrl(animeUrlList: List<AnimeNameURL>?): String {
        return gson.toJson(animeUrlList)
    }

    @TypeConverter
    fun toNameUrl(animeUrlList: String?): List<AnimeNameURL>? {
        if (animeUrlList != null) {
            val animeNameUrl = object : TypeToken<List<AnimeNameURL>>() {}.type
            return gson.fromJson(animeUrlList, animeNameUrl)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromGenre(animeGenreList: List<AnimeGenre>?): String {
        return gson.toJson(animeGenreList)
    }

    @TypeConverter
    fun toGenre(animeGenreList: String?): List<AnimeGenre>? {
        if (animeGenreList != null) {
            val animeGenre = object : TypeToken<List<AnimeGenre>>() {}.type
            return gson.fromJson(animeGenreList, animeGenre)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromRelation(animeRelationList: List<AnimeRelation>?): String {
        return gson.toJson(animeRelationList)
    }

    @TypeConverter
    fun toRelation(animeRelationList: String?): List<AnimeRelation>? {
        if (animeRelationList != null) {
            val animeRelation = object : TypeToken<List<AnimeRelation>>() {}.type
            return gson.fromJson(animeRelationList, animeRelation)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromCharacter(animeCharacterList: List<AnimeCharacter>?): String {
        return gson.toJson(animeCharacterList)
    }

    @TypeConverter
    fun toCharacter(animeCharacterList: String?): List<AnimeCharacter>? {
        if (animeCharacterList != null) {
            val animeCharacter = object : TypeToken<List<AnimeCharacter>>() {}.type
            return gson.fromJson(animeCharacterList, animeCharacter)
        }
        return emptyList()
    }
}