package com.mrntlu.projectconsumer.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Response

//TODO Implement and test with pagination
// https://github.com/codinginflow/SimpleCachingExample/blob/Part-4_Room-Cache/app/src/main/java/com/codinginflow/simplecachingexample/data/RestaurantRepository.kt
// https://github.com/MrNtlu/PassVault/blob/master/app/src/main/java/com/mrntlu/PassVault/utils/NetworkBoundResource.kt
// https://github.com/skydoves/Pokedex/blob/main/core-data/src/main/kotlin/com/skydoves/core/data/repository/MainRepositoryImpl.kt
inline fun <EntityType, ModelType, ReturnType> networkBoundResource(
    isPaginating: Boolean,
    crossinline cacheQuery: () -> EntityType,
    crossinline fetchNetwork: suspend () -> Response<ModelType>,
    crossinline saveAndQueryResult: suspend (ModelType) -> Pair<EntityType, Boolean>,
    crossinline emptyObjectCreator: () -> ReturnType,
    crossinline mapper: (EntityType) -> ReturnType,
    crossinline shouldFetch: (EntityType) -> Boolean = { true }
) = flow<NetworkListResponse<ReturnType>> {
    val data = cacheQuery()

    printLog("CacheQuery $data\n")
    val networkListResponse: NetworkListResponse<ReturnType> = if (shouldFetch(data)) {
        emit(NetworkListResponse.Loading(isPaginating = isPaginating))

        try {
            val response = fetchNetwork()

            printLog("Response ${response.isSuccessful} ${response.body()} ${response.errorBody()}\n")
            if (response.isSuccessful && response.body() != null) {
                val result = saveAndQueryResult(response.body()!!)

                printLog("Result ${result.first} ${result.second}\n")
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
            printLog("Catch Error $throwable\n")
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
        printLog("Should not fetch\n")
        NetworkListResponse.Success(mapper(data), isPaginationData = isPaginating)
    }

    printLog("Final response ${if (networkListResponse is NetworkListResponse.Success) { 
        Pair(networkListResponse.isPaginationData, networkListResponse.isPaginationExhausted)
    } else networkListResponse}")
    emit(networkListResponse)
}.flowOn(Dispatchers.IO)