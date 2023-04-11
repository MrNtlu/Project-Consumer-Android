package com.mrntlu.projectconsumer.repository

import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val movieApiService: MovieApiService,
    private val movieDao: MovieDao,
) {

    //TODO parse error message and return with code
    fun fetchUpcomingMovies(
        page: Int,
        sort: String,
    ) = flow {
        val isPaginating = page != 1

        emit(NetworkListResponse.Loading(isPaginating))

        try {
            val response = movieApiService.getUpcomingMovies(page, sort)

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                val isPaginationExhausted = page >= responseBody.pagination.totalPage

                emit(NetworkListResponse.Success(
                    responseBody.data,
                    isPaginationData = isPaginating,
                    isPaginationExhausted = isPaginationExhausted
                ))
            } else {
                emit(handlePaginationError<Movie>(isPaginating, response.message()))
            }
        } catch (e: Exception) {
            emit(handlePaginationError(isPaginating, e.message ?: e.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun fetchPaginationTest(page: Int) = networkBoundResource(
        query = {

        },
        fetch = {

        },
        saveFetchResult = { movies ->

        }
    )

    private fun<T> handlePaginationError(isPaginating: Boolean, errorMessage: String): NetworkListResponse<List<T>> {
        return if (isPaginating) {
            NetworkListResponse.Success(
                listOf(),
                isPaginationData = true,
                isPaginationExhausted = true
            )
        } else {
            NetworkListResponse.Failure(errorMessage)
        }
    }
}