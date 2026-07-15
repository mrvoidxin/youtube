package com.youtubeclone.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.observeNetworkConnectivity(): Flow<Boolean> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)
        }

        override fun onLost(network: Network) {
            trySend(false)
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    connectivityManager.registerNetworkCallback(request, callback)

    // Initial state
    trySend(isNetworkAvailable())

    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()

fun Context.shareVideo(videoId: String, title: String) {
    val url = "${Constants.YOUTUBE_SHORT_URL}$videoId"
    val shareText = "$title\n$url"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    startActivity(Intent.createChooser(intent, "Share via"))
}

fun Context.openInYouTube(videoId: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Constants.YOUTUBE_BASE_URL}watch?v=$videoId"))
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        startActivity(browserIntent)
    }
}

fun Context.openChannelInYouTube(channelId: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Constants.YOUTUBE_BASE_URL}channel/$channelId"))
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/$channelId"))
        startActivity(browserIntent)
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

fun String?.orDefault(default: String = ""): String = this ?: default

fun String.truncate(maxChars: Int): String {
    return if (this.length > maxChars) {
        this.take(maxChars - 3) + "..."
    } else {
        this
    }
}

fun String.toUri(): Uri = Uri.parse(this)

suspend fun Animatable<Float, *>.bounceTo(targetValue: Float) {
    animateTo(
        targetValue = targetValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
}

fun CoroutineScope.launchBounce(animatable: Animatable<Float, *>, target: Float) {
    launch {
        animatable.bounceTo(target)
    }
}
