package com.youtubeclone.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search?query={query}") {
        fun createRoute(query: String) = "search?query=$query"
    }
    object Watch : Screen("watch/{videoId}") {
        fun createRoute(videoId: String) = "watch/$videoId"
    }
    object Channel : Screen("channel/{channelId}") {
        fun createRoute(channelId: String) = "channel/$channelId"
    }
    object Shorts : Screen("shorts")
    object Library : Screen("library")
    object Subscriptions : Screen("subscriptions")
    object History : Screen("history")
    object LikedVideos : Screen("liked_videos")
    object WatchLater : Screen("watch_later")
    object Settings : Screen("settings")
}
