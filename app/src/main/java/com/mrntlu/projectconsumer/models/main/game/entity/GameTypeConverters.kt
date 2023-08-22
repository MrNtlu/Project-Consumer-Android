package com.mrntlu.projectconsumer.models.main.game.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.main.game.GameMetacriticScorePlatform
import com.mrntlu.projectconsumer.models.main.game.GameRelation
import com.mrntlu.projectconsumer.models.main.game.GameStore

class GameTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return gson.toJson(stringList)
    }

    @TypeConverter
    fun toStringList(stringList: String?): List<String> {
        if (stringList != null) {
            val string = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(stringList, string)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromGameStore(gameStoreList: List<GameStore>?): String {
        return gson.toJson(gameStoreList)
    }

    @TypeConverter
    fun toGameStore(gameStoreList: String?): List<GameStore>? {
        if (gameStoreList != null) {
            val gameStore = object : TypeToken<List<GameStore>>() {}.type
            return gson.fromJson(gameStoreList, gameStore)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromGameMetacriticScorePlatform(gameMetacriticScorePlatformList: List<GameMetacriticScorePlatform>): String {
        return gson.toJson(gameMetacriticScorePlatformList)
    }

    @TypeConverter
    fun toGameMetacriticScorePlatform(gameMetacriticScorePlatformList: String?): List<GameMetacriticScorePlatform> {
        if (gameMetacriticScorePlatformList != null) {
            val gameMetacriticScorePlatform = object : TypeToken<List<GameMetacriticScorePlatform>>() {}.type
            return gson.fromJson(gameMetacriticScorePlatformList, gameMetacriticScorePlatform)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromGameRelation(gameRelationList: List<GameRelation>): String {
        return gson.toJson(gameRelationList)
    }

    @TypeConverter
    fun toGameRelation(gameRelationList: String?): List<GameRelation> {
        if (gameRelationList != null) {
            val gameRelation = object : TypeToken<List<GameRelation>>() {}.type
            return gson.fromJson(gameRelationList, gameRelation)
        }
        return emptyList()
    }
}