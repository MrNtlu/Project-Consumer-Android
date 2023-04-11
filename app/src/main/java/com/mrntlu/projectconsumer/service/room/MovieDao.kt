package com.mrntlu.projectconsumer.service.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.mrntlu.projectconsumer.models.main.movie.Movie

@Dao
interface MovieDao {

    //TODO
    // Find a way to cache every endpoint
    // Delete Caches by time(?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieList(movieList: List<Movie>)
}