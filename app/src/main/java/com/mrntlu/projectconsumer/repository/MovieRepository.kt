package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.movie.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.utils.networkBoundResource
import javax.inject.Inject

private const val UPCOMING_TAG = "upcoming"

class MovieRepository @Inject constructor(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao,
    private val cacheDatabase: CacheDatabase,
) {

    //TODO parse error message and return with code
    //TODO Test error and check if we are getting data from cache or network
    fun fetchUpcomingMovies(page: Int, sort: String) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            movieDao.getMoviesByTag(UPCOMING_TAG, page, sort)
        },
        fetchNetwork = {
            movieApiService.getUpcomingMovies(page, sort)
        },
        mapper = {
             it.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            cacheDatabase.withTransaction {
                movieDao.deleteMoviesByTag(UPCOMING_TAG)
                movieDao.insertMovieList(movieResponse.data.asEntity(UPCOMING_TAG, page))
                Pair(
                    movieDao.getMoviesByTag(UPCOMING_TAG, page, sort),
                    page >= movieResponse.pagination.totalPage
                )
            }
        }
    )
}