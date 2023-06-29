package com.mrntlu.projectconsumer.models.main.userList

import com.google.gson.annotations.SerializedName

data class UserList(
    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userID: String,

    @SerializedName("is_public")
    val isPublic: Boolean,

    @SerializedName("anime_count")
    val animeCount: Int,

    @SerializedName("movie_count")
    val movieCount: Int,

    @SerializedName("tv_count")
    val tvCount: Int,

    @SerializedName("game_count")
    val gameCount: Int,

    @SerializedName("anime_total_watched_episodes")
    val animeTotalWatchedEpisodes: Int,

    @SerializedName("tv_total_watched_episodes")
    val tvTotalWatchedEpisodes: Int,

    @SerializedName("anime_total_finished")
    val animeTotalFinished: Int,

    @SerializedName("game_total_finished")
    val gameTotalFinished: Int,

    @SerializedName("movie_total_finished")
    val movieTotalFinished: Int,

    @SerializedName("tv_total_finished")
    val tvTotalFinished: Int,

    @SerializedName("anime_avg_score")
    val animeAvgScore: Float,

    @SerializedName("game_avg_score")
    val gameAvgScore: Float,

    @SerializedName("movie_avg_score")
    val movieAvgScore: Float,

    @SerializedName("tv_avg_score")
    val tvAvgScore: Float,

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
