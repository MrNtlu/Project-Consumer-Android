package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.utils.Constants

interface UserListInteraction: Interaction<UserList> {
    fun onDeletePressed(item: UserList, contentType: Constants.ContentType, position: Int)
    fun onUpdatePressed(item: UserList, contentType: Constants.ContentType, position: Int)
    fun onDetailsPressed(item: UserList, contentType: Constants.ContentType, position: Int)
    fun onEpisodeIncrementPressed(item: UserList, contentType: Constants.ContentType, position: Int)
    fun onSeasonIncrementPressed(item: UserList, contentType: Constants.ContentType, position: Int)
}