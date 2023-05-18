package com.mrntlu.projectconsumer.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

fun<T> networkResponseFlow(call: suspend () -> Response<T>): Flow<NetworkResponse<T>> = flow {
    emit(NetworkResponse.Loading)

    try {
        val response = call()

        if (response.isSuccessful) {
            response.body()?.let { data ->
                emit(NetworkResponse.Success(data))
            }
        } else {
            emit(NetworkResponse.Failure(errorMessage = response.message()))
        }
    } catch (e: Exception) {
        emit(NetworkResponse.Failure(e.message ?: e.toString()))
    }
}.flowOn(Dispatchers.IO)