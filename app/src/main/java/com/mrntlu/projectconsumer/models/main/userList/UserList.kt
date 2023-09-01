package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class UserList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userID: String,

    @SerializedName("is_public")
    val isPublic: Boolean,

    @SerializedName("anime_list")
    var animeList: List<AnimeList>,

    @SerializedName("game_list")
    var gameList: List<GameList>,

    @SerializedName("movie_watch_list")
    var movieList: List<MovieList>,

    @SerializedName("tv_watch_list")
    var tvList: List<TVSeriesList>,

    val slug: String,
)
