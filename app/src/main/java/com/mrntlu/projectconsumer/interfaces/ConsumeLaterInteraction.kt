package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse

interface ConsumeLaterInteraction: Interaction<ConsumeLaterResponse> {
    //TODO Add variables
    fun onRemovePressed()
    fun onAddToListPressed()
}