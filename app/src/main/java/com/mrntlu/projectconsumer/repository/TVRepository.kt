package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.models.main.tv.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.tv.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.TVSeriesDao
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import javax.inject.Inject

class TVRepository @Inject constructor(
    private val tvSeriesApiService: TVSeriesApiService,
    private val tvSeriesDao: TVSeriesDao,
    private val cacheDatabase: CacheDatabase,
) {

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
                else -> tvSeriesApiService.getTVSeriesBySortFilter(
                    page, sort, "released",
                    null, null, null, null, null
                )
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
                if (page == 1) {
                    tvSeriesDao.deleteTVSeriesByTag(tag)
                }

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
                tvSeriesDao.getAllSearchTVSeries("search:tv", page)
            else
                tvSeriesDao.getSearchTVSeries("search:tv", page)
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
                    tvSeriesDao.deleteTVSeriesByTag("search:tv")
                }

                if (tvResponse.data != null)
                    tvSeriesDao.insertTVSeriesList(tvResponse.data.asEntity("search:tv", page))
                Pair(
                    tvSeriesDao.getSearchTVSeries("search:tv", page),
                    page >= tvResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !tvSeriesDao.isTVSeriesPageExist("search:tv", page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}