package com.mrntlu.projectconsumer.interfaces

abstract class UserListContentModel {
    abstract val id: String
    abstract val contentStatus: String
    abstract val score: Int?
    abstract val timesFinished: Int

    abstract val watchedEpisodes: Int?
    abstract val totalEpisodes: Int?
    abstract val watchedSeasons: Int?

    abstract val contentId: String
    abstract val contentExternalId: String

    abstract val imageUrl: String?
    abstract val title: String
    abstract val titleOriginal: String
}