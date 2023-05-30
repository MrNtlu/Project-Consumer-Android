package com.mrntlu.projectconsumer.service.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrntlu.projectconsumer.models.common.entity.Converters
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieEntity
import com.mrntlu.projectconsumer.models.main.movie.entity.MovieTypeConverters
import com.mrntlu.projectconsumer.models.main.tv.entity.TVSeriesEntity
import com.mrntlu.projectconsumer.models.main.tv.entity.TVSeriesTypeConverters

@Database(
    entities = [MovieEntity::class, TVSeriesEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class, MovieTypeConverters::class, TVSeriesTypeConverters::class, )
abstract class CacheDatabase: RoomDatabase() {
    abstract fun getMovieDao(): MovieDao

    abstract fun getTVSeriesDao(): TVSeriesDao
}