package com.mrntlu.projectconsumer.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

inline fun <EntityType, ModelType, ReturnType> networkBoundResource(
    isPaginating: Boolean,
    crossinline cacheQuery: () -> EntityType,
    crossinline fetchNetwork: suspend () -> Response<ModelType>,
    crossinline saveAndQueryResult: suspend (ModelType) -> Pair<EntityType, Boolean>,
    crossinline emptyObjectCreator: () -> ReturnType,
    crossinline mapper: (EntityType) -> ReturnType,
    crossinline isCachePaginationExhausted: suspend () -> Boolean,
    crossinline shouldFetch: (EntityType) -> Boolean = { true }
) = flow<NetworkListResponse<ReturnType>> {

    val data = cacheQuery()

    val networkListResponse: NetworkListResponse<ReturnType> = if (shouldFetch(data)) {
        emit(NetworkListResponse.Loading(isPaginating = isPaginating))

        try {
            val response = fetchNetwork()

            if (response.isSuccessful && response.body() != null) {
                val result = saveAndQueryResult(response.body()!!)

                NetworkListResponse.Success(
                    mapper(result.first),
                    isPaginationData = isPaginating,
                    isPaginationExhausted = result.second
                )
            } else {
                if (isPaginating) {
                    NetworkListResponse.Success(
                        mapper(data),
                        isPaginationData = true,
                        isPaginationExhausted = true
                    )
                } else {
                    NetworkListResponse.Failure(response.message())
                }
            }
        } catch (throwable: Throwable) {
            if (isPaginating) {
                NetworkListResponse.Success(
                    emptyObjectCreator(),
                    isPaginationData = true,
                    isPaginationExhausted = true
                )
            } else {
                 NetworkListResponse.Failure(throwable.message ?: throwable.toString())
            }
        }
    } else {
        NetworkListResponse.Success(mapper(data), isPaginationData = isPaginating, isPaginationExhausted = isCachePaginationExhausted())
    }

    emit(networkListResponse)
}.flowOn(Dispatchers.IO)