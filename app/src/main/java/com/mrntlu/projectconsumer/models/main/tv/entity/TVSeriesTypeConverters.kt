package com.mrntlu.projectconsumer.models.main.tv.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mrntlu.projectconsumer.models.main.tv.Season

class TVSeriesTypeConverters {
    @TypeConverter
    fun fromNetworkList(networks: List<NetworkEntity>?): String {
        return Gson().toJson(networks)
    }

    @TypeConverter
    fun toNetworkList(networks: String?): List<NetworkEntity>? {
        if (networks != null) {
            val network = object : TypeToken<List<NetworkEntity>>() {}.type
            return Gson().fromJson(networks, network)
        }
        return emptyList()
    }

    @TypeConverter
    fun fromSeasonList(seasons: List<Season>?): String {
        return Gson().toJson(seasons)
    }

    @TypeConverter
    fun toSeasonList(seasons: String?): List<Season>? {
        if (seasons != null) {
            val season = object : TypeToken<List<Season>>() {}.type
            return Gson().fromJson(seasons, season)
        }
        return emptyList()
    }
}