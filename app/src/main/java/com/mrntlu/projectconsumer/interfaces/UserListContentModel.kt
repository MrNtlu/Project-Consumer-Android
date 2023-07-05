package com.mrntlu.projectconsumer.interfaces

abstract class UserListContentModel: UserListModel(), DiffUtilComparison<UserListContentModel> {
    abstract override val id: String
    abstract override val contentStatus: String
    abstract override val score: Int?
    abstract override val timesFinished: Int

    abstract override val mainAttribute: Int?
    abstract override val subAttribute: Int?
    abstract val totalEpisodes: Int?
    abstract val totalSeasons: Int?

    abstract override val contentId: String
    abstract override val contentExternalId: String

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
            subAttribute != newItem.subAttribute -> false

            imageUrl != newItem.imageUrl -> false
            title != newItem.title -> false
            titleOriginal != newItem.titleOriginal -> false
            else -> true
        }
    }
}