package com.youtubeclone.data.repository

import com.youtubeclone.data.local.dao.HistoryDao
import com.youtubeclone.data.local.dao.LikedVideoDao
import com.youtubeclone.data.local.dao.WatchLaterDao
import com.youtubeclone.data.local.entities.HistoryEntity
import com.youtubeclone.data.local.entities.LikedVideoEntity
import com.youtubeclone.data.local.entities.WatchLaterEntity
import com.youtubeclone.domain.models.Video
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val historyDao: HistoryDao,
    private val watchLaterDao: WatchLaterDao,
    private val likedVideoDao: LikedVideoDao
) {

    // Watch History
    fun getWatchHistory(): Flow<List<HistoryEntity>> = historyDao.getAll()

    suspend fun addToHistory(video: Video, watchPosition: Long = 0L) {
        historyDao.insert(
            HistoryEntity(
                videoId = video.id,
                title = video.title,
                thumbnail = video.thumbnailUrl,
                channelName = video.channelTitle,
                channelId = video.channelId,
                duration = video.duration,
                viewCount = video.viewCount,
                watchPosition = watchPosition
            )
        )
    }

    suspend fun removeFromHistory(videoId: String) {
        historyDao.delete(videoId)
    }

    suspend fun clearHistory() {
        historyDao.deleteAll()
    }

    suspend fun isInHistory(videoId: String): Boolean {
        return historyDao.exists(videoId)
    }

    suspend fun updateWatchPosition(videoId: String, position: Long) {
        historyDao.updateWatchPosition(videoId, position)
    }

    fun searchHistory(query: String): Flow<List<HistoryEntity>> {
        return historyDao.search(query)
    }

    // Watch Later
    fun getWatchLater(): Flow<List<WatchLaterEntity>> = watchLaterDao.getAll()

    suspend fun addToWatchLater(video: Video) {
        watchLaterDao.insert(
            WatchLaterEntity(
                videoId = video.id,
                title = video.title,
                thumbnail = video.thumbnailUrl,
                channelName = video.channelTitle,
                channelId = video.channelId,
                duration = video.duration,
                viewCount = video.viewCount
            )
        )
    }

    suspend fun removeFromWatchLater(videoId: String) {
        watchLaterDao.delete(videoId)
    }

    suspend fun isInWatchLater(videoId: String): Boolean {
        return watchLaterDao.exists(videoId)
    }

    // Liked Videos
    fun getLikedVideos(): Flow<List<LikedVideoEntity>> = likedVideoDao.getAll()

    suspend fun addToLikedVideos(video: Video) {
        likedVideoDao.insert(
            LikedVideoEntity(
                videoId = video.id,
                title = video.title,
                thumbnail = video.thumbnailUrl,
                channelName = video.channelTitle,
                channelId = video.channelId,
                duration = video.duration,
                viewCount = video.viewCount
            )
        )
    }

    suspend fun removeFromLikedVideos(videoId: String) {
        likedVideoDao.delete(videoId)
    }

    suspend fun isLiked(videoId: String): Boolean {
        return likedVideoDao.exists(videoId)
    }
}
