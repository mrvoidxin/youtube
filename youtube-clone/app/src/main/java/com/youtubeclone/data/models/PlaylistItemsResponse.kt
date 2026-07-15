package com.youtubeclone.data.models

import com.google.gson.annotations.SerializedName

data class PlaylistItemsResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("prevPageToken") val prevPageToken: String? = null,
    @SerializedName("pageInfo") val pageInfo: PageInfo? = null,
    @SerializedName("items") val items: List<PlaylistItem>? = null
)

data class PlaylistItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: PlaylistItemSnippet? = null,
    @SerializedName("contentDetails") val contentDetails: PlaylistItemContentDetails? = null
)

data class PlaylistItemSnippet(
    @SerializedName("publishedAt") val publishedAt: String? = null,
    @SerializedName("channelId") val channelId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumbnails") val thumbnails: Thumbnails? = null,
    @SerializedName("channelTitle") val channelTitle: String? = null,
    @SerializedName("playlistId") val playlistId: String? = null,
    @SerializedName("position") val position: Int? = null,
    @SerializedName("resourceId") val resourceId: ResourceId? = null
)

data class PlaylistItemContentDetails(
    @SerializedName("videoId") val videoId: String? = null,
    @SerializedName("videoPublishedAt") val videoPublishedAt: String? = null,
    @SerializedName("startAt") val startAt: String? = null,
    @SerializedName("endAt") val endAt: String? = null,
    @SerializedName("note") val note: String? = null
)

data class ResourceId(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("videoId") val videoId: String? = null
)
