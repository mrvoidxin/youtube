package com.youtubeclone.data.models

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("pageInfo") val pageInfo: PageInfo? = null,
    @SerializedName("items") val items: List<CommentThreadItem>? = null
)

data class CommentThreadItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: CommentThreadSnippet? = null,
    @SerializedName("replies") val replies: CommentReplies? = null
)

data class CommentThreadSnippet(
    @SerializedName("videoId") val videoId: String? = null,
    @SerializedName("topLevelComment") val topLevelComment: CommentItem? = null,
    @SerializedName("canReply") val canReply: Boolean? = null,
    @SerializedName("totalReplyCount") val totalReplyCount: Int? = null,
    @SerializedName("isPublic") val isPublic: Boolean? = null
)

data class CommentItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: CommentSnippet? = null
)

data class CommentSnippet(
    @SerializedName("authorDisplayName") val authorDisplayName: String? = null,
    @SerializedName("authorProfileImageUrl") val authorProfileImageUrl: String? = null,
    @SerializedName("authorChannelUrl") val authorChannelUrl: String? = null,
    @SerializedName("authorChannelId") val authorChannelId: ChannelIdValue? = null,
    @SerializedName("textDisplay") val textDisplay: String? = null,
    @SerializedName("textOriginal") val textOriginal: String? = null,
    @SerializedName("likeCount") val likeCount: Int? = null,
    @SerializedName("publishedAt") val publishedAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("viewerRating") val viewerRating: String? = null
)

data class ChannelIdValue(
    @SerializedName("value") val value: String? = null
)

data class CommentReplies(
    @SerializedName("comments") val comments: List<CommentItem>? = null
)
