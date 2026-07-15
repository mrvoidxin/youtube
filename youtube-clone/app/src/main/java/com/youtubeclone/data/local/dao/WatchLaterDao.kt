package com.youtubeclone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.youtubeclone.data.local.entities.WatchLaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchLaterEntity)

    @Query("DELETE FROM watch_later WHERE video_id = :videoId")
    suspend fun delete(videoId: String)

    @Query("DELETE FROM watch_later")
    suspend fun deleteAll()

    @Query("SELECT * FROM watch_later ORDER BY saved_at DESC")
    fun getAll(): Flow<List<WatchLaterEntity>>

    @Query("SELECT * FROM watch_later ORDER BY saved_at DESC")
    suspend fun getAllOnce(): List<WatchLaterEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_later WHERE video_id = :videoId)")
    suspend fun exists(videoId: String): Boolean
}
