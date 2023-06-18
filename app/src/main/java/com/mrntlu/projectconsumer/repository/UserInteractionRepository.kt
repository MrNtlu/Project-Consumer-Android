package com.mrntlu.projectconsumer.repository

import androidx.room.withTransaction
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.mapper.asEntity
import com.mrntlu.projectconsumer.models.main.userInteraction.mapper.asModel
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.service.retrofit.UserInteractionApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.UserInteractionDao
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserInteractionRepository @Inject constructor(
    private val userInteractionApiService: UserInteractionApiService,
    private val userInteractionDao: UserInteractionDao,
    private val cacheDatabase: CacheDatabase,
) {
    private companion object {
        private const val ConsumeLaterTag = "consume-later"
    }

    fun getConsumeLater(type: String?, sort: String, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = false,
        cacheQuery = {
            if (isRestoringData)
                userInteractionDao.getAllConsumeLatersByTag(ConsumeLaterTag, 1, sort)
            else
                userInteractionDao.getConsumeLatersByTag(ConsumeLaterTag, 1, sort)
        },
        fetchNetwork = {
            userInteractionApiService.getConsumeLater(type, sort)
        },
        mapper = {
            it!!.asModel()
        },
        emptyObjectCreator = {
            listOf<ConsumeLaterResponse>()
        },
        saveAndQueryResult = { response ->
            cacheDatabase.withTransaction {
                userInteractionDao.deleteConsumeLatersByTag(ConsumeLaterTag)

                if (response.data != null)
                    userInteractionDao.insertConsumeLaterList(response.data.asEntity(ConsumeLaterTag, 1))
                Pair(
                    userInteractionDao.getConsumeLatersByTag(ConsumeLaterTag, 1, sort),
                    false
                )
            }
        },
        isCachePaginationExhausted = { true },
        shouldFetch = {
            !(isRestoringData && !it.isNullOrEmpty())
        }
    )

    fun createConsumeLater(body: ConsumeLaterBody) = networkResponseFlow {
        userInteractionApiService.createConsumeLater(body)
    }

    fun deleteConsumeLater(body: IDBody) = networkResponseFlow {
        userInteractionApiService.deleteConsumeLater(body)
    }
}