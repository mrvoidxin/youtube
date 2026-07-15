package com.youtubeclone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.youtubeclone.data.local.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<HistoryEntity>)

    @Query("DELETE FROM watch_history WHERE video_id = :videoId")
    suspend fun delete(videoId: String)

    @Query("DELETE FROM watch_history")
    suspend fun deleteAll()

    @Query("SELECT * FROM watch_history ORDER BY watched_at DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY watched_at DESC")
    suspend fun getAllOnce(): List<HistoryEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM watch_history WHERE video_id = :videoId)")
    suspend fun exists(videoId: String): Boolean

    @Query("SELECT * FROM watch_history WHERE title LIKE '%' || :query || '%' OR channel_name LIKE '%' || :query || '%' ORDER BY watched_at DESC")
    fun search(query: String): Flow<List<HistoryEntity>>

    @Query("UPDATE watch_history SET watch_position = :position WHERE video_id = :videoId")
    suspend fun updateWatchPosition(videoId: String, position: Long)
}
