package com.youtubeclone.presentation.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youtubeclone.R
import com.youtubeclone.presentation.theme.YouTubeTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAuthComplete: (Boolean) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var alpha by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 1000),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        alpha = 1f
        delay(2000)
        viewModel.checkAuthStatus()
    }

    LaunchedEffect(viewModel.uiState) {
        when (val state = viewModel.uiState) {
            is SplashUiState.Authenticated -> {
                onAuthComplete(true)
            }
            is SplashUiState.Unauthenticated -> {
                onAuthComplete(false)
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.youtube_logo),
            contentDescription = "YouTube Clone",
            modifier = Modifier
                .size(150.dp)
                .alpha(animatedAlpha),
            colorFilter = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    YouTubeTheme {
        SplashScreen(onAuthComplete = {})
    }
}
