package com.youtubeclone.data.models

import com.google.gson.annotations.SerializedName

data class ChannelResponse(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("pageInfo") val pageInfo: PageInfo? = null,
    @SerializedName("items") val items: List<ChannelItem>? = null
)

data class ChannelItem(
    @SerializedName("kind") val kind: String? = null,
    @SerializedName("etag") val etag: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("snippet") val snippet: ChannelSnippet? = null,
    @SerializedName("statistics") val statistics: ChannelStatistics? = null,
    @SerializedName("brandingSettings") val brandingSettings: ChannelBrandingSettings? = null
)

data class ChannelSnippet(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("customUrl") val customUrl: String? = null,
    @SerializedName("publishedAt") val publishedAt: String? = null,
    @SerializedName("thumbnails") val thumbnails: Thumbnails? = null,
    @SerializedName("defaultLanguage") val defaultLanguage: String? = null,
    @SerializedName("country") val country: String? = null
)

data class ChannelStatistics(
    @SerializedName("viewCount") val viewCount: String? = null,
    @SerializedName("subscriberCount") val subscriberCount: String? = null,
    @SerializedName("hiddenSubscriberCount") val hiddenSubscriberCount: Boolean? = null,
    @SerializedName("videoCount") val videoCount: String? = null
)

data class ChannelBrandingSettings(
    @SerializedName("channel") val channel: ChannelBranding? = null,
    @SerializedName("image") val image: BrandingImage? = null
)

data class ChannelBranding(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("keywords") val keywords: String? = null,
    @SerializedName("country") val country: String? = null
)

data class BrandingImage(
    @SerializedName("bannerExternalUrl") val bannerExternalUrl: String? = null
)
