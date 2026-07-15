package com.youtubeclone.utils

object Constants {
    const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    const val API_KEY = "AIzaSyBPTQbHM2wqBo9BwKIW3FVWYHlrePQzcAY"
    const val YOUTUBE_BASE_URL = "https://www.youtube.com/"
    const val YOUTUBE_SHORT_URL = "https://youtu.be/"

    const val THUMBNAIL_BASE = "https://i.ytimg.com/vi/"
    const val DEFAULT_THUMBNAIL_QUALITY = "mqdefault.jpg"
    const val HIGH_THUMBNAIL_QUALITY = "hqdefault.jpg"
    const val MAX_RES_THUMBNAIL_QUALITY = "maxresdefault.jpg"

    const val PAGE_SIZE = 20
    const val SEARCH_PAGE_SIZE = 20
    const val COMMENT_PAGE_SIZE = 50
    const val SHORTS_PAGE_SIZE = 10
    const val RELATED_PAGE_SIZE = 15

    const val DEBOUNCE_MILLIS = 300L
    const val REWIND_SEEK_MS = 10000L
    const val SHIMMER_ANIMATION_DURATION = 1000
    const val DOUBLE_TAP_TIMEOUT_MS = 300L

    const val HOME_CATEGORY_ALL = "all"
    const val HOME_CATEGORY_MUSIC = "10"  // Music
    const val HOME_CATEGORY_GAMING = "20" // Gaming
    const val HOME_CATEGORY_NEWS = "25"   // News
    const val HOME_CATEGORY_LIVE = "live"
    const val HOME_CATEGORY_LEARNING = "27" // Education
    const val HOME_CATEGORY_SPORTS = "17"   // Sports
    const val HOME_CATEGORY_FASHION = "26"  // Howto & Style
    const val HOME_CATEGORY_PODCASTS = "podcasts"

    val CATEGORIES = listOf(
        "All" to HOME_CATEGORY_ALL,
        "Music" to HOME_CATEGORY_MUSIC,
        "Gaming" to HOME_CATEGORY_GAMING,
        "News" to HOME_CATEGORY_NEWS,
        "Live" to HOME_CATEGORY_LIVE,
        "Learning" to HOME_CATEGORY_LEARNING,
        "Sports" to HOME_CATEGORY_SPORTS,
        "Fashion" to HOME_CATEGORY_FASHION,
        "Podcasts" to HOME_CATEGORY_PODCASTS
    )

    val FILTER_CHIPS = listOf("All", "Videos", "Channels", "Playlists")

    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
    const val TIMEOUT_SECONDS = 30L

    const val DATASTORE_NAME = "youtube_clone_prefs"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_RECENT_SEARCHES = "recent_searches"
    const val KEY_PLAYBACK_SPEED = "playback_speed"

    const val CHANNEL_TABS = listOf("Home", "Videos", "Shorts", "Live", "Playlists", "About")
}
