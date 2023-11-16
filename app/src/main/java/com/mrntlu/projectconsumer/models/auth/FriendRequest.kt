package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.interfaces.DiffUtilComparison
import com.mrntlu.projectconsumer.models.main.review.Author

data class FriendRequest(
    @SerializedName("_id")
    val id: String,

    @SerializedName("is_ignored")
    val isIgnored: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    val sender: Author,
    val receiver: Author,
): DiffUtilComparison<FriendRequest> {
    override fun areItemsTheSame(newItem: FriendRequest): Boolean {
        return id == newItem.id
    }

    override fun areContentsTheSame(newItem: FriendRequest): Boolean {
        return when {
            id != newItem.id -> false
            isIgnored != newItem.isIgnored -> false
            sender.id != newItem.sender.id -> false
            receiver.id != newItem.receiver.id -> false
            else -> true
        }
    }
}
