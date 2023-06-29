package com.mrntlu.projectconsumer.interfaces

abstract class UserListContentModel: DiffUtilComparison<UserListContentModel> {
    abstract val id: String
    abstract val contentStatus: String
    abstract val score: Int?
    abstract val timesFinished: Int

    abstract val mainAttribute: Int?
    abstract val totalEpisodes: Int?
    abstract val watchedSeasons: Int?
    abstract val totalSeasons: Int?

    abstract val contentId: String
    abstract val contentExternalId: String

    abstract val imageUrl: String?
    abstract val title: String
    abstract val titleOriginal: String

    override fun areItemsTheSame(newItem: UserListContentModel): Boolean {
        return id == newItem.id && contentId == newItem.contentId && contentExternalId == newItem.contentExternalId
    }

    override fun areContentsTheSame(newItem: UserListContentModel): Boolean {
        return when {
            id != newItem.id -> false
            contentStatus != newItem.contentStatus -> false
            score != newItem.score -> false

            mainAttribute != newItem.mainAttribute -> false
            totalEpisodes != newItem.totalEpisodes -> false
            watchedSeasons != newItem.watchedSeasons -> false

            imageUrl != newItem.imageUrl -> false
            title != newItem.title -> false
            titleOriginal != newItem.titleOriginal -> false
            else -> true
        }
    }
}