package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.models.main.game.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.game.mapper.asModel
import com.mrntlu.projectconsumer.service.retrofit.GameApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.GameDao
import com.mrntlu.projectconsumer.utils.Constants.SortGameRequests
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class GameRepository @Inject constructor(
    private val gameApiService: GameApiService,
    private val gameDao: GameDao,
    private val cacheDatabase: CacheDatabase,
) {
    private companion object {
        private const val SearchTag = "search:game"
        private const val DiscoverTag = "discover:game"
    }

    fun fetchGames(page: Int, sort: String, tag: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                gameDao.getAllGamesByTag(tag, page, sort)
            else
                gameDao.getGamesByTag(tag, page, sort)
        },
        fetchNetwork = {
            when(tag) {
                FetchType.UPCOMING.tag -> gameApiService.getUpcomingGames(page)
                FetchType.TOP.tag -> gameApiService.getGamesBySortFilter(
                    page, SortGameRequests[1].request, false, null, null
                )
                else -> gameApiService.getGamesBySortFilter(
                    page, SortGameRequests[0].request, null, null, null
                )
            }
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Game>()
        },
        saveAndQueryResult = { gameResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    gameDao.deleteGamesByTag(tag)

                gameDao.insertGameList(gameResponse.data.asEntity(tag, page))
                Pair(
                    gameDao.getGamesByTag(tag, page, sort),
                    page >= gameResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !gameDao.isGamePageExist(tag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun searchGamesByTitle(search: String, page: Int, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                gameDao.getAllSearchGames(SearchTag, page)
            else
                gameDao.getSearchGames(SearchTag, page)
        },
        fetchNetwork = {
            gameApiService.searchGamesByTitle(search, page)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Game>()
        },
        saveAndQueryResult = { gameResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    gameDao.deleteGamesByTag(SearchTag)

                if (gameResponse.data != null)
                    gameDao.insertGameList(gameResponse.data.asEntity(SearchTag, page))
                Pair(
                    gameDao.getSearchGames(SearchTag, page),
                    page >= gameResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !gameDao.isGamePageExist(SearchTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun getGameDetails(id: String) = networkResponseFlow {
        gameApiService.getGameDetails(id)
    }

    fun getGameBySortFilter(
        page: Int,
        sort: String,
        tba: Boolean?,
        genres: String?,
        platform: String?,
        isNetworkAvailable: Boolean,
        isRestoringData: Boolean = false
    ) = networkBoundResource(
        isPaginating = page != 1,
        cacheQuery = {
            if (isRestoringData)
                gameDao.getAllGamesByTag(DiscoverTag, page, sort)
            else
                gameDao.getGamesByTag(DiscoverTag, page, sort)
        },
        fetchNetwork = {
            gameApiService.getGamesBySortFilter(page, sort, tba, genres, platform)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<Game>()
        },
        saveAndQueryResult = { gameResponse ->
            cacheDatabase.withTransaction {
                if (page == 1)
                    gameDao.deleteGamesByTag(DiscoverTag)

                try {
                    gameDao.insertGameList(gameResponse.data.asEntity(DiscoverTag, page))
                } catch (_: Exception){}

                Pair(
                    gameDao.getGamesByTag(DiscoverTag, page, sort),
                    page >= gameResponse.pagination.totalPage
                )
            }
        },
        isCachePaginationExhausted = {
            !gameDao.isGamePageExist(DiscoverTag, page.plus(1))
        },
        shouldFetch = {
            !(
                (isRestoringData && !it.isNullOrEmpty()) ||
                (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )
}