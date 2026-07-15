package com.youtubeclone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.youtubeclone.data.local.entities.LikedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LikedVideoEntity)

    @Query("DELETE FROM liked_videos WHERE video_id = :videoId")
    suspend fun delete(videoId: String)

    @Query("DELETE FROM liked_videos")
    suspend fun deleteAll()

    @Query("SELECT * FROM liked_videos ORDER BY liked_at DESC")
    fun getAll(): Flow<List<LikedVideoEntity>>

    @Query("SELECT * FROM liked_videos ORDER BY liked_at DESC")
    suspend fun getAllOnce(): List<LikedVideoEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_videos WHERE video_id = :videoId)")
    suspend fun exists(videoId: String): Boolean
}
