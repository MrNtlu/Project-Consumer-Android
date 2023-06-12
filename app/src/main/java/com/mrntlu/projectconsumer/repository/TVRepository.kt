package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.models.main.tv.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.tv.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.TVSeriesDao
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class TVRepository @Inject constructor(
    private val tvSeriesApiService: TVSeriesApiService,
    private val tvSeriesDao: TVSeriesDao,
    private val cacheDatabase: CacheDatabase,
) {

    private companion object {
        private const val SearchTag = "search:tv"
        private const val DiscoverTag = "discover:tv"
    }

    fun fetchTVSeries(page: Int, sort: String, tag: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                tvSeriesDao.getAllTVSeriesByTag(tag, page, sort)
            else
                tvSeriesDao.getTVSeriesByTag(tag, page, sort)
        },
        fetchNetwork = {
            when(tag) {
                FetchType.UPCOMING.tag -> tvSeriesApiService.getUpcomingTVSeries(page, sort)
                FetchType.TOP.tag -> tvSeriesApiService.getTopRatedTVSeries(page)
                else -> {
                    if (sort == Constants.SortRequests[0].request)
                        tvSeriesApiService.getPopularTVSeries(page)
                    else
                        tvSeriesApiService.getTVSeriesBySortFilter(
                            page, sort, null,
                            null, null, null, null, null
                        )
                }
            }
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<TVSeries>()
        },
        saveAndQueryResult = { tvResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    tvSeriesDao.deleteTVSeriesByTag(tag)

                tvSeriesDao.insertTVSeriesList(tvResponse.data.asEntity(tag, page))
                Pair(
                    tvSeriesDao.getTVSeriesByTag(tag, page, sort),
                    page >= tvResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !tvSeriesDao.isTVSeriesPageExist(tag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun searchTVSeriesByTitle(search: String, page: Int, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                tvSeriesDao.getAllSearchTVSeries(SearchTag, page)
            else
                tvSeriesDao.getSearchTVSeries(SearchTag, page)
        },
        fetchNetwork = {
            tvSeriesApiService.searchTVSeriesByTitle(search, page)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<TVSeries>()
        },
        saveAndQueryResult = { tvResponse ->
            cacheDatabase.withTransaction {
                if (page == 1) {
                    tvSeriesDao.deleteTVSeriesByTag(SearchTag)
                }

                if (tvResponse.data != null)
                    tvSeriesDao.insertTVSeriesList(tvResponse.data.asEntity(SearchTag, page))
                Pair(
                    tvSeriesDao.getSearchTVSeries(SearchTag, page),
                    page >= tvResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !tvSeriesDao.isTVSeriesPageExist(SearchTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun getTVSeriesDetails(id: String) = networkResponseFlow {
        tvSeriesApiService.getTVSeriesDetails(id)
    }

    fun getTVSeriesBySortFilter(
        page: Int,
        sort: String,
        status: String?,
        numOfSeason: String?,
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
                 tvSeriesDao.getAllTVSeriesByTag(DiscoverTag, page, sort)
            else
                tvSeriesDao.getTVSeriesByTag(DiscoverTag, page, sort)
        },
        fetchNetwork = {
            tvSeriesApiService.getTVSeriesBySortFilter(page, sort, status, numOfSeason, productionCompanies, genres, releaseDateFrom, releaseDateTo)
        },
        mapper = {
             it!!.asModel()
        },
        emptyObjectCreator = {
             listOf<TVSeries>()
        },
        saveAndQueryResult = { tvResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    tvSeriesDao.deleteTVSeriesByTag(DiscoverTag)

                try {
                    tvSeriesDao.insertTVSeriesList(tvResponse.data.asEntity(DiscoverTag, page))
                } catch (_: Exception){}
                Pair(
                    tvSeriesDao.getTVSeriesByTag(DiscoverTag, page, sort),
                    page >= tvResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !tvSeriesDao.isTVSeriesPageExist(DiscoverTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}