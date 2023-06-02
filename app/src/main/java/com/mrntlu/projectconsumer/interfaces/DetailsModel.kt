package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater

abstract class DetailsModel<WatchList> {
    abstract var watchList: WatchList?
    abstract var consumeLater: ConsumeLater?
}