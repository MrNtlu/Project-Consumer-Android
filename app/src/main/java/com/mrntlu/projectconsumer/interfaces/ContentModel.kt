package com.mrntlu.projectconsumer.interfaces

abstract class ContentModel: DiffUtilComparison<ContentModel> {
    abstract val id: String
    abstract val imageURL: String
    abstract val title: String
    abstract val titleOriginal: String
    abstract val description: String

    override fun areItemsTheSame(newItem: ContentModel): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: ContentModel): Boolean {
        return when {
            id != newItem.id -> false
            imageURL != newItem.imageURL -> false
            title != newItem.title -> false
            titleOriginal != newItem.titleOriginal -> false
            description != newItem.description -> false
            else -> true
        }
    }
}