package com.mrntlu.projectconsumer.di

import com.mrntlu.projectconsumer.repository.AuthRepository
import com.mrntlu.projectconsumer.repository.MoviePreviewRepository
import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.repository.TVPreviewRepository
import com.mrntlu.projectconsumer.repository.TVRepository
import com.mrntlu.projectconsumer.repository.UserListRepository
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.service.retrofit.AuthApiService
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import com.mrntlu.projectconsumer.service.retrofit.TVSeriesApiService
import com.mrntlu.projectconsumer.service.retrofit.UserApiService
import com.mrntlu.projectconsumer.service.retrofit.UserListApiService
import com.mrntlu.projectconsumer.service.room.CacheDatabase
import com.mrntlu.projectconsumer.service.room.MovieDao
import com.mrntlu.projectconsumer.service.room.TVSeriesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class HiltModule {

    @Provides
    fun provideTVSeriesRepository(
        tvSeriesApiService: TVSeriesApiService, tvSeriesDao: TVSeriesDao, cacheDatabase: CacheDatabase
    ) = TVRepository(tvSeriesApiService, tvSeriesDao, cacheDatabase)

    @Provides
    fun provideTVSeriesPreviewRepository(tvSeriesApiService: TVSeriesApiService) = TVPreviewRepository(tvSeriesApiService)

    @Provides
    fun provideMovieRepository(
        movieApiService: MovieApiService, movieDao: MovieDao, cacheDatabase: CacheDatabase
    ) = MovieRepository(movieApiService, movieDao, cacheDatabase)

    @Provides
    fun provideMoviePreviewRepository(movieApiService: MovieApiService) = MoviePreviewRepository(movieApiService)

    @Provides
    fun provideUserListRepository(userListApiService: UserListApiService) = UserListRepository(userListApiService)

    @Provides
    fun provideAuthRepository(authApiService: AuthApiService) = AuthRepository(authApiService)

    @Provides
    fun provideUserRepository(userApiService: UserApiService) = UserRepository(userApiService)
}