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

    //TODO Search

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
//               FetchType.TOP.tag -> animeApiService.getUpcomingAnimes(page, sort) TODO FIX THIS
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

    fun getAnimeDetails(id: String) = networkResponseFlow {
        animeApiService.getAnimeDetails(id)
    }

    fun getMovieBySortFilter(
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