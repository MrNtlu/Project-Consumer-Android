package com.mrntlu.projectconsumer.interfaces

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