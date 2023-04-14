package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.movie.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.printLog
import kotlinx.coroutines.delay
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
            delay(3000L)
            printLog("Fetching $page")
            movieApiService.getUpcomingMovies(page, sort)
        },
        mapper = {
             it.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            printLog("Data Size ${movieResponse.data.count()}")

            cacheDatabase.withTransaction {
                if (page == 1) {
                    movieDao.deleteMoviesByTag(UPCOMING_TAG)
                } else {
                    movieDao.deleteMoviesByTagAndPage(UPCOMING_TAG, page)
                }
                movieDao.insertMovieList(movieResponse.data.asEntity(UPCOMING_TAG, page))
                Pair(
                    movieDao.getMoviesByTag(UPCOMING_TAG, page, sort),
                    page >= movieResponse.pagination.totalPage
                )
            }
        }
    )
}