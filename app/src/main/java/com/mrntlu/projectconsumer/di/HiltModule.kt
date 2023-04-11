package com.mrntlu.projectconsumer.di

import com.mrntlu.projectconsumer.repository.MovieRepository
import com.mrntlu.projectconsumer.service.retrofit.MovieApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class HiltModule {

    @Provides
    fun provideMovieRepository(movieApiService: MovieApiService) = MovieRepository(movieApiService)
}