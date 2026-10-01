package com.youtubeclone.di

import com.youtubeclone.data.api.BackendApiService
import com.youtubeclone.data.api.YouTubeApiService
import com.youtubeclone.data.local.dao.HistoryDao
import com.youtubeclone.data.local.dao.LikedVideoDao
import com.youtubeclone.data.local.dao.WatchLaterDao
import com.youtubeclone.data.repository.AuthRepository
import com.youtubeclone.data.repository.BackendVideoRepository
import com.youtubeclone.data.repository.LocalRepository
import com.youtubeclone.data.repository.SearchRepository
import com.youtubeclone.data.repository.VideoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: BackendApiService): AuthRepository {
        return AuthRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideBackendVideoRepository(apiService: BackendApiService): BackendVideoRepository {
        return BackendVideoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideVideoRepository(apiService: YouTubeApiService): VideoRepository {
        return VideoRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: YouTubeApiService): SearchRepository {
        return SearchRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideLocalRepository(
        historyDao: HistoryDao,
        watchLaterDao: WatchLaterDao,
        likedVideoDao: LikedVideoDao
    ): LocalRepository {
        return LocalRepository(historyDao, watchLaterDao, likedVideoDao)
    }
}
