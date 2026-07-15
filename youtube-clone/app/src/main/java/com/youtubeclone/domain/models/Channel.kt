package com.youtubeclone.domain.models

import com.youtubeclone.data.models.ChannelItem

data class Channel(
    val id: String,
    val title: String,
    val description: String,
    val customUrl: String,
    val thumbnailUrl: String,
    val bannerUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val viewCount: Long,
    val publishedAt: String,
    val country: String,
    val hiddenSubscriberCount: Boolean
)

fun ChannelItem.toDomain(): Channel? {
    if (id == null) return null
    return Channel(
        id = id ?: "",
        title = snippet?.title.orEmpty(),
        description = snippet?.description.orEmpty(),
        customUrl = snippet?.customUrl.orEmpty(),
        thumbnailUrl = snippet?.thumbnails?.high?.url
            ?: snippet?.thumbnails?.medium?.url
            ?: snippet?.thumbnails?.default?.url.orEmpty(),
        bannerUrl = brandingSettings?.image?.bannerExternalUrl.orEmpty(),
        subscriberCount = statistics?.subscriberCount?.toLongOrNull() ?: 0L,
        videoCount = statistics?.videoCount?.toLongOrNull() ?: 0L,
        viewCount = statistics?.viewCount?.toLongOrNull() ?: 0L,
        publishedAt = snippet?.publishedAt.orEmpty(),
        country = snippet?.country.orEmpty(),
        hiddenSubscriberCount = statistics?.hiddenSubscriberCount ?: false
    )
}
