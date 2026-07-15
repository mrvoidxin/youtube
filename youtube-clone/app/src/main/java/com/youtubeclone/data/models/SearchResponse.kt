package com.youtubeclone.data.models

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("prevPageToken") val prevPageToken: String? = null,
    @SerializedName("regionCode") val regionCode: String? = null,
    @SerializedName("pageInfo") val pageInfo: PageInfo? = null,
    @SerializedName("items") val items: List<SearchItem>? = null
)

data class SearchItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: SearchItemId? = null,
    @SerializedName("snippet") val snippet: VideoSnippet? = null
)

data class SearchItemId(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("videoId") val videoId: String? = null,
    @SerializedName("channelId") val channelId: String? = null,
    @SerializedName("playlistId") val playlistId: String? = null
)
