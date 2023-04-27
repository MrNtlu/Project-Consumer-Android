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
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao,
    private val cacheDatabase: CacheDatabase,
) {

    //TODO parse error message and return with code
    //TODO Test error and check if we are getting data from cache or network
    fun fetchMovies(page: Int, sort: String, tag: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            movieDao.getMoviesByTag(tag, page, sort)
        },
        fetchNetwork = {
            when(tag) {
                FetchType.UPCOMING.tag -> movieApiService.getUpcomingMovies(page, sort)
                else -> movieApiService.getMovieBySortFilter(page, sort, "released", null, null, null, null)
            }
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            cacheDatabase.withTransaction {
                if (page == 1) {
                    movieDao.deleteMoviesByTag(tag)
                }

                movieDao.insertMovieList(movieResponse.data.asEntity(tag, page))
                Pair(
                    movieDao.getMoviesByTag(tag, page, sort),
                    page >= movieResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !movieDao.isMoviePageExist(tag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}