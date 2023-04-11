package com.mrntlu.projectconsumer.service.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrntlu.projectconsumer.models.main.movie.Movie

@Database(
    entities = [Movie::class],
    version = 1,
    exportSchema = false,
)
abstract class CacheDatabase: RoomDatabase() {
    abstract fun getMovieDao(): MovieDao
}