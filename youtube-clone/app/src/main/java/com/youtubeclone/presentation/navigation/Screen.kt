package com.youtubeclone.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Shorts : Screen("shorts")
    data object Library : Screen("library")
    data object Search : Screen("search")
    data object Watch : Screen("watch/{videoId}") {
        fun createRoute(videoId: String) = "watch/$videoId"
    }
    data object Channel : Screen("channel/{channelId}") {
        fun createRoute(channelId: String) = "channel/$channelId"
    }
    data object History : Screen("history")
    data object WatchLater : Screen("watch_later")
    data object LikedVideos : Screen("liked_videos")
    data object Subscriptions : Screen("subscriptions")
    data object Settings : Screen("settings")
}
