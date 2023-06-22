package com.mrntlu.projectconsumer.models.auth

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("is_premium")
    val isPremium: Boolean,

    @SerializedName("anime_count")
    val animeCount: Int,

    @SerializedName("game_count")
    val gameCount: Int,

    @SerializedName("movie_count")
    val movieCount: Int,

    @SerializedName("tv_count")
    val tvCount: Int,

    @SerializedName("fcm_token")
    val fcmToken: String,

    @SerializedName("legend_anime_list")
    val legendAnimeList: List<UserInfoCommon>,

    @SerializedName("legend_movie_list")
    val legendMovieList: List<UserInfoCommon>,

    @SerializedName("legend_tv_list")
    val legendTVList: List<UserInfoCommon>,

    @SerializedName("legend_game_list")
    val legendGameList: List<UserInfoGame>,

    val username: String,
    val email: String,
    val image: String?,
)
