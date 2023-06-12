package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.movie.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.movie.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao,
    private val cacheDatabase: CacheDatabase,
) {
    private companion object {
        private const val SearchTag = "search:movie"
        private const val DiscoverTag = "discover:movie"
    }

    fun fetchMovies(page: Int, sort: String, tag: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                movieDao.getAllMoviesByTag(tag, page, sort)
            else
                movieDao.getMoviesByTag(tag, page, sort)
        },
        fetchNetwork = {
            when(tag) {
                FetchType.UPCOMING.tag -> movieApiService.getUpcomingMovies(page, sort)
                FetchType.TOP.tag -> movieApiService.getTopRatedMovies(page)
                else -> movieApiService.getMovieBySortFilter(
                    page, sort, Constants.MovieStatusRequests[1].request,
                    null, null, null, null
                )
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
                if (page == 1)
                    movieDao.deleteMoviesByTag(tag)

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

    fun searchMoviesByTitle(search: String, page: Int, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                movieDao.getAllSearchMovies(SearchTag, page)
            else
                movieDao.getSearchMovies(SearchTag, page)
        },
        fetchNetwork = {
            movieApiService.searchMoviesByTitle(search, page)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    movieDao.deleteMoviesByTag(SearchTag)

                if (movieResponse.data != null)
                    movieDao.insertMovieList(movieResponse.data.asEntity(SearchTag, page))
                Pair(
                    movieDao.getSearchMovies(SearchTag, page),
                    page >= movieResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !movieDao.isMoviePageExist(SearchTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun getMovieDetails(id: String) = networkResponseFlow {
        movieApiService.getMovieDetails(id)
    }

    fun getMovieBySortFilter(
        page: Int,
        sort: String,
        status: String?,
        productionCompanies: String?,
        genres: String?,
        releaseDateFrom: Int?,
        releaseDateTo: Int?,
        isNetworkAvailable: Boolean,
        isRestoringData: Boolean = false
    ) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                movieDao.getAllMoviesByTag(DiscoverTag, page, sort)
            else
                movieDao.getMoviesByTag(DiscoverTag, page, sort)
        },
        fetchNetwork = {
            movieApiService.getMovieBySortFilter(page, sort, status, productionCompanies, genres, releaseDateFrom, releaseDateTo)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Movie>()
        },
        saveAndQueryResult = { movieResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    movieDao.deleteMoviesByTag(DiscoverTag)

                try {
                    movieDao.insertMovieList(movieResponse.data.asEntity(DiscoverTag, page))
                } catch (_: Exception){}

                Pair(
                    movieDao.getMoviesByTag(DiscoverTag, page, sort),
                    page >= movieResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !movieDao.isMoviePageExist(DiscoverTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}