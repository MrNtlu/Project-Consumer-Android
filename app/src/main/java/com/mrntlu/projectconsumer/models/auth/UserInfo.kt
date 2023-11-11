package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse

data class UserInfo(
    @SerializedName("_id")
    val id: String,

    @SerializedName("is_premium")
    val isPremium: Boolean,

    @SerializedName("membership_type")
    val membershipType: Int,

    @SerializedName("anime_count")
    val animeCount: Int,

    @SerializedName("game_count")
    val gameCount: Int,

    @SerializedName("movie_count")
    val movieCount: Int,

    @SerializedName("tv_count")
    val tvCount: Int,

    @SerializedName("movie_watched_time")
    val movieWatchedTime: Long,

    @SerializedName("anime_watched_episodes")
    val animeWatchedEpisodes: Long,

    @SerializedName("tv_watched_episodes")
    val tvWatchedEpisodes: Long,

    @SerializedName("game_total_hours_played")
    val gameTotalHoursPlayed: Long,

    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("legend_content")
    val legendContent: List<UserInfoCommon>,

    @SerializedName("consume_later")
    val watchLater: List<ConsumeLaterResponse>?,

    val username: String,
    val email: String,
    val image: String?,
    val level: Int,
    val reviews: List<ReviewWithContent>?,
)
