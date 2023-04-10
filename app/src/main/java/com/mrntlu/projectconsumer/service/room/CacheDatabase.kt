package com.mrntlu.projectconsumer.service.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    exportSchema = false,
)
abstract class CacheDatabase: RoomDatabase() {
    abstract fun getCacheDao(): CacheDao
}