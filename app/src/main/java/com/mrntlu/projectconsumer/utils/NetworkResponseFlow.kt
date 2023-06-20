package com.mrntlu.projectconsumer.utils

import com.google.gson.Gson
import com.mrntlu.projectconsumer.models.common.retrofit.ErrorAltResponse
import com.mrntlu.projectconsumer.models.common.retrofit.ErrorResponse
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
            val errorBody = response.errorBody()?.string()

            val error = if (errorBody != null) {
                var errorJson: String? = Gson().fromJson(errorBody, ErrorResponse::class.java)?.message
                if (errorJson == null)
                   errorJson = Gson().fromJson(errorBody, ErrorAltResponse::class.java)?.error

                errorJson ?: "Unknown error!"
            } else
                response.message()

            emit(NetworkResponse.Failure(errorMessage = error))
        }
    } catch (e: Exception) {
        emit(NetworkResponse.Failure(e.message ?: e.toString()))
    }
}.flowOn(Dispatchers.IO)