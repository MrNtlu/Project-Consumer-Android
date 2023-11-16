package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.auth.FriendRequest

interface FriendRequestInteraction: Interaction<FriendRequest> {
    fun onAcceptClicked(item: FriendRequest, position: Int)
    fun onIgnoreClicked(item: FriendRequest, position: Int)
    fun onDenyClicked(item: FriendRequest, position: Int)
}