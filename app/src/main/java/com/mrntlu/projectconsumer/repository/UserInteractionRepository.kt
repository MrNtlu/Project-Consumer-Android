package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.service.retrofit.UserInteractionApiService
import com.mrntlu.projectconsumer.utils.networkResponseFlow
import javax.inject.Inject

class UserInteractionRepository @Inject constructor(
    private val userInteractionApiService: UserInteractionApiService,
) {

    fun createConsumeLater(body: ConsumeLaterBody) = networkResponseFlow {
        userInteractionApiService.createConsumeLater(body)
    }

    fun deleteConsumeLater(body: IDBody) = networkResponseFlow {
        userInteractionApiService.deleteConsumeLater(body)
    }
}