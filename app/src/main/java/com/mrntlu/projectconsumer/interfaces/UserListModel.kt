package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList

abstract class UserListModel {
    abstract val id: String
    abstract val score: Int?
    abstract val timesFinished: Int
    abstract val contentStatus: String

    abstract val mainAttribute: Int?
    abstract val subAttribute: Int?

    abstract val contentId: String
    abstract val contentExternalId: String
}

fun UserListModel.toTvWatchList() = TVSeriesWatchList(
    id, contentId, contentExternalId, timesFinished, mainAttribute ?: 0,
    subAttribute ?: 0, contentStatus, score
)

fun UserListModel.toMovieWatchList() = MovieWatchList(
    id, contentId, contentExternalId, timesFinished,
    contentStatus, score,
)