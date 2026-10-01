package com.youtubeclone.di

import com.youtubeclone.data.local.PreferencesManager
import com.youtubeclone.data.local.TokenManager
import com.youtubeclone.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideTokenManager(
        preferencesManager: PreferencesManager,
        authRepository: AuthRepository
    ): TokenManager {
        return TokenManager(preferencesManager, authRepository)
    }
}
