package com.youtubeclone.data.api

import com.youtubeclone.data.models.ChannelResponse
import com.youtubeclone.data.models.CommentResponse
import com.youtubeclone.data.models.SearchResponse
import com.youtubeclone.data.models.VideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("videos")
    suspend fun getTrendingVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics,liveStreamingDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("regionCode") regionCode: String = "US",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("videoCategoryId") videoCategoryId: String? = null
    ): Response<VideoResponse>

    @GET("videos")
    suspend fun getVideosById(
        @Query("part") part: String = "snippet,contentDetails,statistics,liveStreamingDetails",
        @Query("id") videoId: String,
        @Query("maxResults") maxResults: Int = 1
    ): Response<VideoResponse>

    @GET("videos")
    suspend fun getMultipleVideos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") videoIds: String,
        @Query("maxResults") maxResults: Int = 50
    ): Response<VideoResponse>

    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("order") order: String = "relevance",
        @Query("videoDuration") videoDuration: String? = null,
        @Query("regionCode") regionCode: String = "US",
        @Query("safeSearch") safeSearch: String = "moderate"
    ): Response<SearchResponse>

    @GET("search")
    suspend fun searchAll(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("order") order: String = "relevance",
        @Query("regionCode") regionCode: String = "US",
        @Query("safeSearch") safeSearch: String = "moderate"
    ): Response<SearchResponse>

    @GET("search")
    suspend fun getRelatedVideos(
        @Query("part") part: String = "snippet",
        @Query("relatedToVideoId") relatedToVideoId: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 15,
        @Query("pageToken") pageToken: String? = null
    ): Response<SearchResponse>

    @GET("search")
    suspend fun searchShorts(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("videoDuration") videoDuration: String = "short",
        @Query("maxResults") maxResults: Int = 10,
        @Query("pageToken") pageToken: String? = null,
        @Query("regionCode") regionCode: String = "US"
    ): Response<SearchResponse>

    @GET("channels")
    suspend fun getChannelDetails(
        @Query("part") part: String = "snippet,statistics,brandingSettings",
        @Query("id") channelId: String
    ): Response<ChannelResponse>

    @GET("channels")
    suspend fun getMultipleChannels(
        @Query("part") part: String = "snippet,statistics,brandingSettings",
        @Query("id") channelIds: String,
        @Query("maxResults") maxResults: Int = 50
    ): Response<ChannelResponse>

    @GET("commentThreads")
    suspend fun getVideoComments(
        @Query("part") part: String = "snippet,replies",
        @Query("videoId") videoId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("order") order: String = "relevance"
    ): Response<CommentResponse>

    @GET("playlistItems")
    suspend fun getPlaylistVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null
    ): Response<com.youtubeclone.data.models.PlaylistItemsResponse>

    @GET("search")
    suspend fun searchChannels(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "channel",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("regionCode") regionCode: String = "US"
    ): Response<SearchResponse>

    @GET("search")
    suspend fun searchPlaylists(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "playlist",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("regionCode") regionCode: String = "US"
    ): Response<SearchResponse>
}
