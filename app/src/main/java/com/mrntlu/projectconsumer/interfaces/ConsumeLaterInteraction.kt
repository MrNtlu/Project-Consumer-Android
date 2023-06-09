package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse

interface ConsumeLaterInteraction: Interaction<ConsumeLaterResponse> {
    fun onDeletePressed(item: ConsumeLaterResponse, position: Int)
    fun onAddToListPressed(item: ConsumeLaterResponse, position: Int)
    fun onDiscoverButtonPressed()
}