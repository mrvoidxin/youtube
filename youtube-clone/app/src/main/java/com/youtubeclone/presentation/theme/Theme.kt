package com.youtubeclone.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = TextPrimary,
    primaryContainer = BgElevated,
    onPrimaryContainer = TextPrimary,
    secondary = BgSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = BgElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = LikeBlue,
    onTertiary = TextPrimary,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgPrimary,
    onSurface = TextPrimary,
    surfaceVariant = BgSecondary,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    outlineVariant = BgElevated,
    error = AccentRed,
    onError = TextPrimary,
    scrim = OverlayBlack60
)

private val LightColorScheme = lightColorScheme(
    primary = AccentRed,
    onPrimary = TextPrimary,
    primaryContainer = BgElevatedLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = BgSecondaryLight,
    onSecondary = TextPrimaryLight,
    secondaryContainer = BgElevatedLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = LikeBlue,
    onTertiary = TextPrimary,
    background = BgPrimaryLight,
    onBackground = TextPrimaryLight,
    surface = BgPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BgSecondaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerColorLight,
    outlineVariant = BgElevatedLight,
    error = AccentRed,
    onError = TextPrimary,
    scrim = OverlayBlack60
)

@Composable
fun YouTubeCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) BgPrimary.toArgb() else BgPrimaryLight.toArgb()
            window.navigationBarColor = if (darkTheme) BgPrimary.toArgb() else BgPrimaryLight.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YouTubeTypography,
        content = content
    )
}
