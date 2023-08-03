package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.anime.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.anime.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.AnimeApiService
import com.mrntlu.projectconsumer.service.room.AnimeDao
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class AnimeRepository @Inject constructor(
    private val animeApiService: AnimeApiService,
    private val animeDao: AnimeDao,
    private val cacheDatabase: CacheDatabase,
) {
    private companion object {
        private const val SearchTag = "search:anime"
        private const val DiscoverTag = "discover:anime"
    }

    fun fetchAnimes(page: Int, sort: String, tag: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                animeDao.getAllAnimesByTag(tag, page, sort)
            else
                animeDao.getAnimesByTag(tag, page, sort)
        },
        fetchNetwork = {
            when(tag) {
               FetchType.UPCOMING.tag -> animeApiService.getUpcomingAnimes(page, sort)
               FetchType.POPULAR.tag -> animeApiService.getPopularAnimes(page)
                else -> animeApiService.getAnimesBySortFilter(
                    page, sort, Constants.AnimeStatusRequests[2].request,
                    null, null, null, null,
                )
            }
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Anime>()
        },
        saveAndQueryResult = { animeResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    animeDao.deleteAnimesByTag(tag)

                animeDao.insertAnimeList(animeResponse.data.asEntity(tag, page))
                Pair(
                    animeDao.getAnimesByTag(tag, page, sort),
                    page >= animeResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !animeDao.isAnimePageExist(tag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun searchAnimesByTitle(search: String, page: Int, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                animeDao.getAllSearchAnimes(SearchTag, page)
            else
                animeDao.getSearchAnimes(SearchTag, page)
        },
        fetchNetwork = {
            animeApiService.searchAnimesByTitle(search, page)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Anime>()
        },
        saveAndQueryResult = { animeResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    animeDao.deleteAnimesByTag(SearchTag)

                if (animeResponse.data != null)
                    animeDao.insertAnimeList(animeResponse.data.asEntity(SearchTag, page))
                Pair(
                    animeDao.getSearchAnimes(SearchTag, page),
                    page >= animeResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !animeDao.isAnimePageExist(SearchTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun getAnimeDetails(id: String) = networkResponseFlow {
        animeApiService.getAnimeDetails(id)
    }

    fun getAnimeBySortFilter(
        page: Int,
        sort: String,
        status: String?,
        genres: String?,
        demographics: String?,
        themes: String?,
        studios: String?,
        isNetworkAvailable: Boolean,
        isRestoringData: Boolean = false
    ) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                animeDao.getAllAnimesByTag(DiscoverTag, page, sort)
            else
                animeDao.getAnimesByTag(DiscoverTag, page, sort)
        },
        fetchNetwork = {
            animeApiService.getAnimesBySortFilter(page, sort, status, genres, demographics, themes, studios)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Anime>()
        },
        saveAndQueryResult = { animeResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    animeDao.deleteAnimesByTag(DiscoverTag)

                try {
                    animeDao.insertAnimeList(animeResponse.data.asEntity(DiscoverTag, page))
                } catch (_: Exception){}

                Pair(
                    animeDao.getAnimesByTag(DiscoverTag, page, sort),
                    page >= animeResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !animeDao.isAnimePageExist(DiscoverTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}