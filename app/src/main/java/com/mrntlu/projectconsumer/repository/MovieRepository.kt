package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.movie.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.printLog
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao,
    private val cacheDatabase: CacheDatabase,
) {

    //TODO parse error message and return with code
    //TODO Test error and check if we are getting data from cache or network
    fun fetchUpcomingMovies(page: Int, sort: String, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            movieDao.getMoviesByTag(FetchType.UPCOMING.tag, page, sort)
        },
        fetchNetwork = {
//            delay(4000L)
            movieApiService.getUpcomingMovies(page, sort)
        },
        mapper = {
             it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            printLog("Data Size ${movieResponse.data.count()}")

            cacheDatabase.withTransaction {
                if (page == 1) {
                    movieDao.deleteMoviesByTag(FetchType.UPCOMING.tag)
                }

                movieDao.insertMovieList(movieResponse.data.asEntity(FetchType.UPCOMING.tag, page))
                Pair(
                    movieDao.getMoviesByTag(FetchType.UPCOMING.tag, page, sort),
                    page >= movieResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !movieDao.isMoviePageExist(FetchType.UPCOMING.tag, page.plus(1))
        },
        shouldFetch = {
            !(isRestoringData && !it.isNullOrEmpty()) // TODO Add internet check
        }
    )
}