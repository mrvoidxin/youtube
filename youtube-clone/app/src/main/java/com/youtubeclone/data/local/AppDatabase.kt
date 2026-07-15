package com.youtubeclone.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.youtubeclone.data.local.dao.HistoryDao
import com.youtubeclone.data.local.dao.LikedVideoDao
import com.youtubeclone.data.local.dao.WatchLaterDao
import com.youtubeclone.data.local.entities.HistoryEntity
import com.youtubeclone.data.local.entities.LikedVideoEntity
import com.youtubeclone.data.local.entities.WatchLaterEntity

@Database(
    entities = [
        HistoryEntity::class,
        WatchLaterEntity::class,
        LikedVideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun watchLaterDao(): WatchLaterDao
    abstract fun likedVideoDao(): LikedVideoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "youtube_clone_db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
