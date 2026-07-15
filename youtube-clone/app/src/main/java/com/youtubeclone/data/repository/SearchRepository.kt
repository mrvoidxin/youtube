package com.youtubeclone.data.repository

import com.youtubeclone.data.api.YouTubeApiService
import com.youtubeclone.domain.models.Video
import com.youtubeclone.domain.models.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val apiService: YouTubeApiService
) {

    suspend fun searchVideos(
        query: String,
        pageToken: String? = null,
        type: String = "video"
    ): Result<SearchResult> {
        return try {
            val response = when (type) {
                "channel" -> apiService.searchChannels(query = query, pageToken = pageToken)
                "playlist" -> apiService.searchPlaylists(query = query, pageToken = pageToken)
                else -> apiService.searchVideos(query = query, pageToken = pageToken)
            }
            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    SearchResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken
                    )
                )
            } else {
                Result.failure(Exception("Search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAll(
        query: String,
        pageToken: String? = null
    ): Result<SearchResult> {
        return try {
            val response = apiService.searchAll(query = query, pageToken = pageToken)
            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    SearchResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken
                    )
                )
            } else {
                Result.failure(Exception("Search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchShorts(
        query: String,
        pageToken: String? = null
    ): Result<SearchResult> {
        return try {
            val response = apiService.searchShorts(query = query, pageToken = pageToken)
            if (response.isSuccessful) {
                val body = response.body()
                val videos = body?.items?.mapNotNull { it.toDomain() } ?: emptyList()
                Result.success(
                    SearchResult(
                        videos = videos,
                        nextPageToken = body?.nextPageToken
                    )
                )
            } else {
                Result.failure(Exception("Shorts search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class SearchResult(
        val videos: List<Video>,
        val nextPageToken: String?
    )
}
