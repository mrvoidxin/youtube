package com.youtubeclone.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_videos")
data class LikedVideoEntity(
    @PrimaryKey
    @ColumnInfo(name = "video_id")
    val videoId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String,

    @ColumnInfo(name = "channel_name")
    val channelName: String,

    @ColumnInfo(name = "channel_id")
    val channelId: String,

    @ColumnInfo(name = "duration")
    val duration: String,

    @ColumnInfo(name = "view_count")
    val viewCount: Long,

    @ColumnInfo(name = "liked_at")
    val likedAt: Long = System.currentTimeMillis()
)
