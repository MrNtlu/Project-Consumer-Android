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
        emit(if (isPaginating) setPaginationLoading() else setLoading())

        try {
            val response = fetchNetwork()

            if (response.isSuccessful && response.body() != null) {
                val result = saveAndQueryResult(response.body()!!)

                setData(
                    mapper(result.first),
                    isPaginating,
                    result.second
                )
            } else {
                if (isPaginating) {
                    setData(
                        mapper(data),
                        isPaginationData = true,
                        isPaginationExhausted = true,
                    )
                } else {
                    setFailure(isPaginationData = false, isPaginationExhausted = false, errorMessage = response.message())
                }
            }
        } catch (throwable: Throwable) {
            setFailure(emptyObjectCreator(), isPaginationData = isPaginating, isPaginationExhausted = isPaginating, errorMessage = throwable.message ?: throwable.toString())
        }
    } else {
        setData(mapper(data), isPaginationData = isPaginating, isPaginationExhausted = isCachePaginationExhausted())
    }

    emit(networkListResponse)
}.flowOn(Dispatchers.IO)