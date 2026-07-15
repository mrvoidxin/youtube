package com.youtubeclone.domain.models

import com.youtubeclone.data.models.SearchItem
import com.youtubeclone.data.models.VideoItem

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val highThumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val channelThumbnailUrl: String,
    val publishedAt: String,
    val duration: String,
    val viewCount: Long,
    val likeCount: Long,
    val dislikeCount: Long,
    val commentCount: Long,
    val tags: List<String>,
    val categoryId: String,
    val isLive: Boolean,
    val definition: String,
    val hasCaptions: Boolean,
    val liveConcurrentViewers: Long
)

fun VideoItem.toDomain(): Video? {
    if (id == null) return null
    return Video(
        id = id ?: "",
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default?.url.orEmpty(),
        highThumbnailUrl = snippet?.thumbnails?.maxres?.url
            ?: snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url.orEmpty(),
        channelId = snippet?.channelId.orEmpty(),
        channelTitle = snippet?.channelTitle.orEmpty(),
        channelThumbnailUrl = "",
        publishedAt = snippet?.publishedAt.orEmpty(),
        duration = contentDetails?.duration.orEmpty(),
        viewCount = statistics?.viewCount?.toLongOrNull() ?: 0L,
        likeCount = statistics?.likeCount?.toLongOrNull() ?: 0L,
        dislikeCount = statistics?.dislikeCount?.toLongOrNull() ?: 0L,
        commentCount = statistics?.commentCount?.toLongOrNull() ?: 0L,
        tags = snippet?.tags ?: emptyList(),
        categoryId = snippet?.categoryId.orEmpty(),
        isLive = snippet?.liveBroadcastContent == "live",
        definition = contentDetails?.definition.orEmpty(),
        hasCaptions = contentDetails?.caption == "true",
        liveConcurrentViewers = liveStreamingDetails?.concurrentViewers?.toLongOrNull() ?: 0L
    )
}

fun SearchItem.toDomain(): Video? {
    if (id?.videoId == null) return null
    return Video(
        id = id?.videoId ?: return null,
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default?.url.orEmpty(),
        highThumbnailUrl = snippet?.thumbnails?.maxres?.url
            ?: snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url.orEmpty(),
        channelId = snippet?.channelId.orEmpty(),
        channelTitle = snippet?.channelTitle.orEmpty(),
        channelThumbnailUrl = "",
        publishedAt = snippet?.publishedAt.orEmpty(),
        duration = "",
        viewCount = 0L,
        likeCount = 0L,
        dislikeCount = 0L,
        commentCount = 0L,
        tags = emptyList(),
        categoryId = "",
        isLive = snippet?.liveBroadcastContent == "live",
        definition = "",
        hasCaptions = false,
        liveConcurrentViewers = 0L
    )
}
