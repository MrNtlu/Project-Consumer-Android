package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.service.retrofit.UserInteractionApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.utils.networkBoundResource
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserInteractionRepository @Inject constructor(
    private val userInteractionApiService: UserInteractionApiService,
    private val cacheDatabase: CacheDatabase,
) {

    fun getConsumeLater(type: String?, sort: String, isNetworkAvailable: Boolean, isRestoringData: Boolean = false) = networkBoundResource(
        isPaginating = false,
        cacheQuery = {

        },
        fetchNetwork = {
            userInteractionApiService.getConsumeLater(type, sort)
        },
        mapper = {

        },
        emptyObjectCreator = {
            listOf<ConsumeLaterBody>()
        },
        saveAndQueryResult = { response ->

        },
        isCachePaginationExhausted = { true },
        shouldFetch = {
            !(
                    (isRestoringData && !it.isNullOrEmpty()) ||
                    (!isNetworkAvailable && !it.isNullOrEmpty())
            )
        }
    )

    fun createConsumeLater(body: ConsumeLaterBody) = networkResponseFlow {
        userInteractionApiService.createConsumeLater(body)
    }

    fun deleteConsumeLater(body: IDBody) = networkResponseFlow {
        userInteractionApiService.deleteConsumeLater(body)
    }
}