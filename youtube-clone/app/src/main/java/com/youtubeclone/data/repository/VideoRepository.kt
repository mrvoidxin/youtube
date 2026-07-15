package com.youtubeclone.data.repository

import com.youtubeclone.data.api.YouTubeApiService
import com.youtubeclone.data.models.ChannelResponse
import com.youtubeclone.data.models.CommentResponse
import com.youtubeclone.data.models.SearchResponse
import com.youtubeclone.data.models.VideoResponse
import com.youtubeclone.domain.models.Channel
import com.youtubeclone.domain.models.Comment
import com.youtubeclone.domain.models.Video
import com.youtubeclone.domain.models.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val apiService: YouTubeApiService
) {

    suspend fun getTrendingVideos(
        pageToken: String? = null,
        categoryId: String? = null
    ): Result<TrendingResult> {
        return try {
            val response = apiService.getTrendingVideos(
                pageToken = pageToken,
                videoCategoryId = categoryId
            )
            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch trending videos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVideoDetails(videoId: String): Result<Video> {
        return try {
            val response = apiService.getVideosById(videoId = videoId)
            if (response.isSuccessful) {
                val video = response.body()?.items?.firstOrNull()?.toDomain()
                if (video != null) {
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

    suspend fun getChannelDetails(channelId: String): Result<Channel> {
        return try {
            val response = apiService.getChannelDetails(channelId = channelId)
            if (response.isSuccessful) {
                val channel = response.body()?.items?.firstOrNull()?.toDomain()
                if (channel != null) {
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
        pageToken: String? = null
    ): Result<TrendingResult> {
        return try {
            val response = apiService.searchAll(
                query = "",
                pageToken = pageToken
            )
            if (response.isSuccessful) {
                val body = response.body()
                val channelVideos = body?.items
                    ?.filter { it.snippet?.channelId == channelId }
                    ?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = channelVideos,
                        nextPageToken = body?.nextPageToken
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
        pageToken: String? = null
    ): Result<CommentsResult> {
        return try {
            val response = apiService.getVideoComments(
                videoId = videoId,
                pageToken = pageToken
            )
            if (response.isSuccessful) {
                val body = response.body()
                val comments = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    CommentsResult(
                        comments = comments,
                        nextPageToken = body?.nextPageToken
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
        pageToken: String? = null
    ): Result<TrendingResult> {
        return try {
            val response = apiService.getRelatedVideos(
                relatedToVideoId = videoId,
                pageToken = pageToken
            )
            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    TrendingResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken
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
        val nextPageToken: String?
    )

    data class CommentsResult(
        val comments: List<Comment>,
        val nextPageToken: String?
    )
}
