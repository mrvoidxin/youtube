package com.youtubeclone.data.repository

import com.youtubeclone.data.api.BackendApiService
import com.youtubeclone.domain.models.Channel
import com.youtubeclone.domain.models.Comment
import com.youtubeclone.domain.models.Video
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendVideoRepository @Inject constructor(
    private val apiService: BackendApiService
) {

    suspend fun getTrendingVideos(
        page: Int = 1,
        pageSize: Int = 20,
        categoryId: String? = null,
        accessToken: String? = null
    ): Result<TrendingResult> {
        return try {
            val response = if (accessToken != null) {
                // Add authorization header if available
                apiService.getFeed(page, pageSize, categoryId)
            } else {
                apiService.getFeed(page, pageSize, categoryId)
            }

            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.map { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken,
                        isCached = body?.isCached ?: false,
                        quotaExceeded = body?.quotaExceeded ?: false
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch trending videos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoDetails(
        videoId: String,
        accessToken: String? = null
    ): Result<Video> {
        return try {
            val response = apiService.getVideoDetails(videoId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val video = body.toDomain()
                    Result.success(video)
                } else {
                    Result.failure(Exception("Video not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch video: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelDetails(
        channelId: String,
        accessToken: String? = null
    ): Result<Channel> {
        return try {
            val response = apiService.getChannelDetails(channelId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val channel = body.toDomain()
                    Result.success(channel)
                } else {
                    Result.failure(Exception("Channel not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch channel: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelVideos(
        channelId: String,
        pageToken: String? = null,
        accessToken: String? = null
    ): Result<TrendingResult> {
        return try {
            // For now, use search to get channel videos
            val response = apiService.search(
                q = "",
                page = 1,
                pageSize = 20,
                type = "video",
                categoryId = null
            )

            if (response.isSuccessful) {
                val body = response.body()
                val channelVideos = body?.items?.filter { it.channelId == channelId }?.map { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = channelVideos,
                        nextPageToken = body?.nextPageToken,
                        isCached = body?.isCached ?: false,
                        quotaExceeded = body?.quotaExceeded ?: false
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch channel videos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoComments(
        videoId: String,
        pageToken: String? = null,
        accessToken: String? = null
    ): Result<CommentsResult> {
        return try {
            val response = apiService.getVideoComments(videoId)
            if (response.isSuccessful) {
                val body = response.body()
                val comments = body?.items?.map { it.toDomain() } ?: emptyList()
                Result.success(
                    CommentsResult(
                        comments = comments,
                        nextPageToken = body?.nextPageToken,
                        isCached = body?.isCached ?: false,
                        quotaExceeded = body?.quotaExceeded ?: false
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch comments: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRelatedVideos(
        videoId: String,
        pageToken: String? = null,
        accessToken: String? = null
    ): Result<TrendingResult> {
        return try {
            val response = apiService.getVideoDetails(videoId)
            if (response.isSuccessful) {
                val body = response.body()
                val relatedVideos = body?.relatedVideos?.map { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = relatedVideos,
                        nextPageToken = null,
                        isCached = body?.isCached ?: false,
                        quotaExceeded = body?.quotaExceeded ?: false
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch related videos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class TrendingResult(
        val videos: List<Video>,
        val nextPageToken: String?,
        val isCached: Boolean = false,
        val quotaExceeded: Boolean = false
    )

    data class CommentsResult(
        val comments: List<Comment>,
        val nextPageToken: String?,
        val isCached: Boolean = false,
        val quotaExceeded: Boolean = false
    )
}
