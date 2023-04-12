package com.mrntlu.projectconsumer.service.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrntlu.projectconsumer.models.main.movie.entity.Converters
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieEntity

@Database(
    entities = [MovieEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class CacheDatabase: RoomDatabase() {
    abstract fun getMovieDao(): MovieDao
}