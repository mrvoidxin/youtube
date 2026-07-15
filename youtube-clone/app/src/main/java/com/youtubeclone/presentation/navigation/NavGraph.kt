package com.youtubeclone.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.youtubeclone.presentation.screens.channel.ChannelScreen
import com.youtubeclone.presentation.screens.home.HomeScreen
import com.youtubeclone.presentation.screens.library.HistoryScreen
import com.youtubeclone.presentation.screens.library.LibraryScreen
import com.youtubeclone.presentation.screens.library.LikedVideosScreen
import com.youtubeclone.presentation.screens.library.WatchLaterScreen
import com.youtubeclone.presentation.screens.search.SearchScreen
import com.youtubeclone.presentation.screens.settings.SettingsScreen
import com.youtubeclone.presentation.screens.shorts.ShortsScreen
import com.youtubeclone.presentation.screens.watch.WatchScreen
import com.youtubeclone.presentation.theme.BottomNavBg
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

@Composable
fun YouTubeNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, Screen.Home.route),
        BottomNavItem("Shorts", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow, Screen.Shorts.route),
        BottomNavItem("", Icons.Filled.Add, Icons.Filled.Add, ""),
        BottomNavItem("Subscriptions", Icons.Filled.Notifications, Icons.Outlined.Notifications, Screen.Subscriptions.route),
        BottomNavItem("Library", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, Screen.Library.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Shorts.route,
        Screen.Subscriptions.route,
        Screen.Library.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BottomNavBg,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = when (item.route) {
                            Screen.Home.route -> currentRoute == Screen.Home.route
                            Screen.Shorts.route -> currentRoute == Screen.Shorts.route
                            Screen.Subscriptions.route -> currentRoute == Screen.Subscriptions.route
                            Screen.Library.route -> currentRoute == Screen.Library.route
                            else -> false
                        }

                        if (item.route.isEmpty()) {
                            NavigationBarItem(
                                selected = false,
                                onClick = { },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Create",
                                        tint = TextPrimary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = BottomNavBg
                                )
                            )
                        } else {
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TextPrimary,
                                    unselectedIconColor = TextSecondary,
                                    selectedTextColor = TextPrimary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = BottomNavBg
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(300)
                )
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    },
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onCastClick = { },
                    onNotificationsClick = { }
                )
            }

            composable(Screen.Shorts.route) {
                ShortsScreen(
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onHistoryClick = {
                        navController.navigate(Screen.History.route)
                    },
                    onWatchLaterClick = {
                        navController.navigate(Screen.WatchLater.route)
                    },
                    onLikedVideosClick = {
                        navController.navigate(Screen.LikedVideos.route)
                    },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    }
                )
            }

            composable(Screen.Subscriptions.route) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Subscriptions",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    },
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    }
                )
            }

            composable(
                route = Screen.Watch.route,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) {
                WatchScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { id ->
                        navController.navigate(Screen.Watch.createRoute(id))
                    },
                    onChannelClick = { channelId ->
                        navController.navigate(Screen.Channel.createRoute(channelId))
                    }
                )
            }

            composable(
                route = Screen.Channel.route,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) {
                ChannelScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    }
                )
            }

            composable(Screen.WatchLater.route) {
                WatchLaterScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    }
                )
            }

            composable(Screen.LikedVideos.route) {
                LikedVideosScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId ->
                        navController.navigate(Screen.Watch.createRoute(videoId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
