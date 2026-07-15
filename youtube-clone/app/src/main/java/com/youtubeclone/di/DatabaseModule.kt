package com.youtubeclone.di

import android.content.Context
import com.youtubeclone.data.local.AppDatabase
import com.youtubeclone.data.local.dao.HistoryDao
import com.youtubeclone.data.local.dao.LikedVideoDao
import com.youtubeclone.data.local.dao.WatchLaterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideWatchLaterDao(database: AppDatabase): WatchLaterDao {
        return database.watchLaterDao()
    }

    @Provides
    @Singleton
    fun provideLikedVideoDao(database: AppDatabase): LikedVideoDao {
        return database.likedVideoDao()
    }
}
