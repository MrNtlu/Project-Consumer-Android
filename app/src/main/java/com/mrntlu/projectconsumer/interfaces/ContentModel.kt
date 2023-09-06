package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.anime.AnimeAirDate

abstract class ContentModel: DiffUtilComparison<ContentModel> {
    abstract val id: String
    abstract val imageURL: String
    abstract val title: String
    abstract val titleOriginal: String
    abstract val description: String
    abstract val score: Float
    abstract val releaseDate: String?
    abstract val episodes: Int?
    abstract val totalSeasons: Int?
    abstract val length: Int?
    abstract val aired: AnimeAirDate?

    override fun areItemsTheSame(newItem: ContentModel): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: ContentModel): Boolean {
        return when {
            id != newItem.id -> false
            imageURL != newItem.imageURL -> false
            title != newItem.title -> false
            titleOriginal != newItem.titleOriginal -> false
            else -> true
        }
    }
}