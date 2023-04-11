package com.mrntlu.projectconsumer.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

//https://github.com/MrNtlu/PassVault/blob/master/app/src/main/java/com/mrntlu/PassVault/utils/NetworkBoundResource.kt

//TODO Implement and test with pagination
inline fun <ResultType, RequestType> networkBoundResource(
    page: Int,
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> ResultType,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    val isPaginating = page != 1
    val data = query().first()

    val flow = if (shouldFetch(data)) {
        emit(NetworkListResponse.Loading(isPaginating = isPaginating))

        try {
            saveFetchResult(fetch())
            query().map { NetworkListResponse.Success(it, isPaginationData = isPaginating) }
        } catch (throwable: Throwable) {
            query().map { NetworkListResponse.Failure(throwable.message ?: throwable.toString()) }
        }
    } else {
        query().map { NetworkListResponse.Success(it, isPaginationData = isPaginating) }
    }

    emitAll(flow)
}.flowOn(Dispatchers.IO)