package com.youtubeclone.data.models

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("items") val items: List<VideoItem>? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("prevPageToken") val prevPageToken: String? = null,
    @SerializedName("pageInfo") val pageInfo: PageInfo? = null
)

data class VideoItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: VideoSnippet? = null,
    @SerializedName("contentDetails") val contentDetails: ContentDetails? = null,
    @SerializedName("statistics") val statistics: VideoStatistics? = null,
    @SerializedName("liveStreamingDetails") val liveStreamingDetails: LiveStreamingDetails? = null
)

data class VideoSnippet(
    @SerializedName("publishedAt") val publishedAt: String? = null,
    @SerializedName("channelId") val channelId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnails") val thumbnails: Thumbnails? = null,
    @SerializedName("channelTitle") val channelTitle: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("liveBroadcastContent") val liveBroadcastContent: String? = null,
    @SerializedName("defaultLanguage") val defaultLanguage: String? = null,
    @SerializedName("localized") val localized: Localized? = null
)

data class ContentDetails(
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("dimension") val dimension: String? = null,
    @SerializedName("definition") val definition: String? = null,
    @SerializedName("caption") val caption: String? = null,
    @SerializedName("licensedContent") val licensedContent: Boolean? = null,
    @SerializedName("contentRating") val contentRating: ContentRating? = null,
    @SerializedName("projection") val projection: String? = null
)

data class VideoStatistics(
    @SerializedName("viewCount") val viewCount: String? = null,
    @SerializedName("likeCount") val likeCount: String? = null,
    @SerializedName("dislikeCount") val dislikeCount: String? = null,
    @SerializedName("favoriteCount") val favoriteCount: String? = null,
    @SerializedName("commentCount") val commentCount: String? = null
)

data class Thumbnails(
    @SerializedName("default") val default: ThumbnailInfo? = null,
    @SerializedName("medium") val medium: ThumbnailInfo? = null,
    @SerializedName("high") val high: ThumbnailInfo? = null,
    @SerializedName("standard") val standard: ThumbnailInfo? = null,
    @SerializedName("maxres") val maxres: ThumbnailInfo? = null
)

data class ThumbnailInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("width") val width: Int? = null,
    @SerializedName("height") val height: Int? = null
)

data class LiveStreamingDetails(
    @SerializedName("actualStartTime") val actualStartTime: String? = null,
    @SerializedName("actualEndTime") val actualEndTime: String? = null,
    @SerializedName("scheduledStartTime") val scheduledStartTime: String? = null,
    @SerializedName("concurrentViewers") val concurrentViewers: String? = null,
    @SerializedName("activeLiveChatId") val activeLiveChatId: String? = null
)

data class Localized(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null
)

data class ContentRating(
    @SerializedName("ytRating") val ytRating: String? = null
)

data class PageInfo(
    @SerializedName("totalResults") val totalResults: Int? = null,
    @SerializedName("resultsPerPage") val resultsPerPage: Int? = null
)
