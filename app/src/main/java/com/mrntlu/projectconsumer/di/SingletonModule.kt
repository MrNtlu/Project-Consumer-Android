package com.mrntlu.projectconsumer.di

import android.content.Context
import androidx.room.Room
import com.mrntlu.projectconsumer.service.AuthAuthenticator
import com.mrntlu.projectconsumer.service.AuthInterceptor
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.service.retrofit.AuthApiService
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.service.retrofit.UserApiService
import com.mrntlu.projectconsumer.service.retrofit.UserInteractionApiService
import com.mrntlu.projectconsumer.service.retrofit.UserListApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.service.room.TVSeriesDao
import com.mrntlu.projectconsumer.service.room.UserInteractionDao
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager = TokenManager(context)

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor =
        AuthInterceptor(tokenManager)

    @Singleton
    @Provides
    fun provideAuthAuthenticator(tokenManager: TokenManager, authApiService: AuthApiService): AuthAuthenticator =
        AuthAuthenticator(tokenManager, authApiService)

    @Singleton
    @Provides
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context) = NetworkConnectivityObserver(context)

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
//            .addInterceptor(loggingInterceptor)
            .authenticator(authAuthenticator)
            .build()
    }

    @Singleton
    @Provides
    @Named("auth_okhttp")
    fun provideAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .addConverterFactory(GsonConverterFactory.create())

    @Singleton
    @Provides
    fun provideUserListAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): UserListApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(UserListApiService::class.java)

    @Singleton
    @Provides
    fun provideUserInteractionAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): UserInteractionApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(UserInteractionApiService::class.java)

    @Singleton
    @Provides
    fun provideMovieAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): MovieApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(MovieApiService::class.java)

    @Singleton
    @Provides
    fun provideTVSeriesAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): TVSeriesApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(TVSeriesApiService::class.java)

    @Singleton
    @Provides
    fun provideAuthAPIService(@Named("auth_okhttp") okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): AuthApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(AuthApiService::class.java)

    @Singleton
    @Provides
    fun provideUserAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): UserApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(UserApiService::class.java)

    @Singleton
    @Provides
    fun provideCacheDatabase(@ApplicationContext context: Context): CacheDatabase =
        Room
            .databaseBuilder(context, CacheDatabase::class.java, Constants.CACHE_DB_NAME)
            .build()

    @Singleton
    @Provides
    fun provideMovieDao(cacheDB: CacheDatabase): MovieDao = cacheDB.getMovieDao()

    @Singleton
    @Provides
    fun provideTVSeriesDao(cacheDB: CacheDatabase): TVSeriesDao = cacheDB.getTVSeriesDao()

    @Singleton
    @Provides
    fun provideUserInteractionDao(cacheDB: CacheDatabase): UserInteractionDao = cacheDB.getUserInteractionDao()
}