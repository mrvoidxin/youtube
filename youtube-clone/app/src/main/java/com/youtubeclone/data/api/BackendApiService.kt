package com.youtubeclone.data.api

import com.youtubeclone.data.models.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface BackendApiService {

    // ============================================
    // AUTHENTICATION
    // ============================================

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @GET("auth/me")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String
    ): Response<UserProfileResponse>

    // ============================================
    // FEED
    // ============================================

    @GET("feed")
    suspend fun getFeed(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("categoryId") categoryId: String? = null
    ): Response<FeedResponse>

    // ============================================
    // SEARCH
    // ============================================

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("type") type: String? = null,
        @Query("duration") duration: String? = null,
        @Query("order") order: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): Response<SearchResponse>

    // ============================================
    // VIDEOS
    // ============================================

    @GET("videos/{id}")
    suspend fun getVideoDetails(
        @Path("id") videoId: String
    ): Response<VideoDetailsResponse>

    // ============================================
    // CHANNELS
    // ============================================

    @GET("channels/{id}")
    suspend fun getChannelDetails(
        @Path("id") channelId: String
    ): Response<ChannelDetailsResponse>

    @POST("channels/{id}/subscribe")
    suspend fun subscribeToChannel(
        @Path("id") channelId: String,
        @Header("Authorization") authHeader: String
    ): Response<SubscriptionResponse>

    @DELETE("channels/{id}/subscribe")
    suspend fun unsubscribeFromChannel(
        @Path("id") channelId: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    // ============================================
    // COMMENTS
    // ============================================

    @GET("comments/videos/{videoId}")
    suspend fun getVideoComments(
        @Path("videoId") videoId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("order") order: String? = null
    ): Response<CommentsResponse>

    @POST("comments/videos/{videoId}")
    suspend fun createComment(
        @Path("videoId") videoId: String,
        @Body request: CreateCommentRequest,
        @Header("Authorization") authHeader: String
    ): Response<CommentResponse>

    @POST("comments/{id}/like")
    suspend fun likeComment(
        @Path("id") commentId: String,
        @Header("Authorization") authHeader: String
    ): Response<LikeResponse>

    // ============================================
    // HISTORY
    // ============================================

    @GET("history")
    suspend fun getWatchHistory(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") authHeader: String
    ): Response<HistoryResponse>

    @POST("history")
    suspend fun addToHistory(
        @Body request: CreateHistoryRequest,
        @Header("Authorization") authHeader: String
    ): Response<HistoryItemResponse>

    @DELETE("history")
    suspend fun clearHistory(
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @DELETE("history/{id}")
    suspend fun removeFromHistory(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>
}

// ============================================
// REQUEST/RESPONSE MODELS
// ============================================

// Authentication

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val credential: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val user: UserProfileResponse,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

data class UserProfileResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val createdAt: String
)

// Feed

data class FeedResponse(
    val items: List<VideoDomain>,
    val nextPageToken: String?,
    val totalResults: Int,
    val isCached: Boolean,
    val quotaExceeded: Boolean? = null
)

// Search

data class SearchResponse(
    val items: List<SearchResultDomain>,
    val nextPageToken: String?,
    val totalResults: Int,
    val isCached: Boolean,
    val quotaExceeded: Boolean? = null
)

// Videos

data class VideoDetailsResponse(
    val id: String,
    val youtubeVideoId: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val duration: String?,
    val viewCount: Long,
    val likeCount: Long,
    val dislikeCount: Long,
    val commentCount: Long,
    val publishedAt: String?,
    val isShort: Boolean,
    val categoryId: String?,
    val tags: List<String>,
    val channel: ChannelDomain?,
    val relatedVideos: List<VideoDomain>,
    val isLiked: Boolean,
    val isDisliked: Boolean,
    val isSubscribed: Boolean,
    val isInWatchLater: Boolean,
    val isInHistory: Boolean,
    val isCached: Boolean,
    val quotaExceeded: Boolean? = null
)

// Channels

data class ChannelDetailsResponse(
    val id: String,
    val youtubeChannelId: String,
    val name: String,
    val bannerUrl: String?,
    val avatarUrl: String?,
    val description: String?,
    val subscriberCount: Long,
    val viewCount: Long,
    val videoCount: Long,
    val isSubscribed: Boolean,
    val subscriberCountText: String,
    val videos: List<VideoDomain>,
    val isCached: Boolean,
    val quotaExceeded: Boolean? = null
)

data class SubscriptionResponse(
    val message: String,
    val subscription: SubscriptionDomain
)

// Comments

data class CreateCommentRequest(
    val body: String,
    val parentCommentId: String? = null
)

data class CommentsResponse(
    val items: List<CommentDomain>,
    val nextPageToken: String?,
    val totalResults: Int,
    val isCached: Boolean,
    val quotaExceeded: Boolean? = null
)

data class CommentResponse(
    val id: String,
    val youtubeCommentId: String?,
    val videoId: String,
    val user: UserProfileResponse,
    val body: String,
    val likeCount: Int,
    val createdAt: String,
    val replies: List<CommentDomain>,
    val isLiked: Boolean,
    val isDisliked: Boolean
)

data class LikeResponse(
    val message: String,
    val like: LikeDomain?
)

// History

data class CreateHistoryRequest(
    val videoId: String,
    val progressSeconds: Int = 0,
    val duration: Int? = null
)

data class HistoryResponse(
    val items: List<HistoryItemDomain>,
    val totalResults: Int,
    val page: Int,
    val pageSize: Int
)

data class HistoryItemResponse(
    val id: String,
    val videoId: String,
    val video: VideoDomain,
    val watchedAt: String,
    val progressSeconds: Int,
    val duration: Int?
)

// Domain Models

data class VideoDomain(
    val id: String,
    val youtubeVideoId: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val duration: String?,
    val viewCount: Long,
    val likeCount: Long,
    val dislikeCount: Long,
    val commentCount: Long,
    val publishedAt: String?,
    val isShort: Boolean,
    val categoryId: String?,
    val tags: List<String>,
    val channel: ChannelDomain? = null,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSubscribed: Boolean = false
)

data class ChannelDomain(
    val id: String,
    val youtubeChannelId: String?,
    val name: String,
    val bannerUrl: String?,
    val avatarUrl: String?,
    val description: String?,
    val subscriberCount: Long,
    val viewCount: Long,
    val videoCount: Long,
    val isSubscribed: Boolean,
    val subscriberCountText: String
)

data class SearchResultDomain(
    val id: String,
    val youtubeVideoId: String? = null,
    val youtubeChannelId: String? = null,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val duration: String?,
    val viewCount: Long? = null,
    val likeCount: Long? = null,
    val dislikeCount: Long? = null,
    val commentCount: Long? = null,
    val publishedAt: String?,
    val isShort: Boolean? = null,
    val categoryId: String?,
    val tags: List<String>? = null,
    val channel: ChannelDomain? = null,
    val channelTitle: String? = null,
    val channelId: String? = null,
    val type: String? = null,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isSubscribed: Boolean = false
)

data class CommentDomain(
    val id: String,
    val youtubeCommentId: String?,
    val videoId: String,
    val user: UserProfileResponse,
    val body: String,
    val likeCount: Int,
    val createdAt: String,
    val replies: List<CommentDomain>,
    val isLiked: Boolean,
    val isDisliked: Boolean
)

data class SubscriptionDomain(
    val id: String,
    val channelId: String,
    val channel: ChannelDomain,
    val createdAt: String
)

data class LikeDomain(
    val id: String,
    val targetType: String,
    val targetId: String,
    val value: String,
    val createdAt: String
)

data class HistoryItemDomain(
    val id: String,
    val videoId: String,
    val video: VideoDomain,
    val watchedAt: String,
    val progressSeconds: Int,
    val duration: Int?
)
