package com.youtubeclone.domain.models

import com.youtubeclone.data.models.CommentThreadItem

data class Comment(
    val id: String,
    val videoId: String,
    val authorDisplayName: String,
    val authorProfileImageUrl: String,
    val authorChannelId: String,
    val textDisplay: String,
    val textOriginal: String,
    val likeCount: Int,
    val replyCount: Int,
    val publishedAt: String,
    val updatedAt: String
)

fun CommentThreadItem.toDomain(): Comment? {
    if (id == null || snippet?.topLevelComment?.id == null) return null
    val commentSnippet = snippet?.topLevelComment?.snippet ?: return null
    return Comment(
        id = snippet?.topLevelComment?.id ?: id ?: "",
        videoId = snippet?.videoId.orEmpty(),
        authorDisplayName = commentSnippet.authorDisplayName.orEmpty(),
        authorProfileImageUrl = commentSnippet.authorProfileImageUrl.orEmpty(),
        authorChannelId = commentSnippet.authorChannelId?.value.orEmpty(),
        textDisplay = commentSnippet.textDisplay.orEmpty(),
        textOriginal = commentSnippet.textOriginal.orEmpty(),
        likeCount = commentSnippet.likeCount ?: 0,
        replyCount = snippet?.totalReplyCount ?: 0,
        publishedAt = commentSnippet.publishedAt.orEmpty(),
        updatedAt = commentSnippet.updatedAt.orEmpty()
    )
}
