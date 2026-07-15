package com.youtubeclone.presentation.screens.watch

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Clipboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.youtubeclone.domain.models.Video
import com.youtubeclone.presentation.components.SubscribeButton
import com.youtubeclone.presentation.components.VideoCard
import com.youtubeclone.presentation.theme.AccentRed
import com.youtubeclone.presentation.theme.BgPrimary
import com.youtubeclone.presentation.theme.LikeBlue
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import com.youtubeclone.presentation.theme.TextTertiary
import com.youtubeclone.utils.FormatUtils

@Composable
fun WatchScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    onChannelClick: (String) -> Unit,
    viewModel: WatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val likeScale = remember { Animatable(1f) }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentRed)
        }
        return
    }

    if (uiState.error != null && uiState.video == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgPrimary),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = uiState.error ?: "Error", color = TextSecondary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to retry",
                    color = AccentRed,
                    modifier = Modifier.clickable { onBack() }
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Video Player
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isFullscreen) Modifier.fillMaxSize()
                        else Modifier.aspectRatio(16f / 9f)
                    )
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { ctx ->
                        YouTubePlayerView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    youTubePlayer.loadVideo(
                                        uiState.video?.id ?: "",
                                        0f
                                    )
                                }

                                override fun onStateChange(
                                    youTubePlayer: YouTubePlayer,
                                    state: PlayerConstants.PlayerState
                                ) {
                                    viewModel.setPlayerState(
                                        when (state) {
                                            PlayerConstants.PlayerState.PLAYING -> PlayerState.PLAYING
                                            PlayerConstants.PlayerState.PAUSED -> PlayerState.PAUSED
                                            PlayerConstants.PlayerState.BUFFERING -> PlayerState.BUFFERING
                                            PlayerConstants.PlayerState.ENDED -> PlayerState.ENDED
                                            else -> PlayerState.IDLE
                                        }
                                    )
                                }
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, _ -> }
                        }
                )
            }
        }

        // Back button overlay
        if (!isFullscreen) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Title
        item {
            uiState.video?.let { video ->
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = video.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (uiState.isDescriptionExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Views and date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = buildString {
                                append(FormatUtils.formatViewCount(video.viewCount.toLong()))
                                append(" · ")
                                append(FormatUtils.formatRelativeTime(video.publishedAt))
                            },
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        IconButton(
                            onClick = { viewModel.toggleDescription() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                if (uiState.isDescriptionExpanded) Icons.Outlined.ExpandLess
                                else Icons.Outlined.ExpandMore,
                                contentDescription = "Expand",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action buttons
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                item {
                    LikeButton(
                        isActive = uiState.isLiked,
                        iconActive = Icons.Filled.ThumbUp,
                        iconInactive = Icons.Outlined.ThumbUp,
                        text = "Like",
                        onClick = { viewModel.toggleLike() }
                    )
                }
                item {
                    LikeButton(
                        isActive = uiState.isDisliked,
                        iconActive = Icons.Filled.ThumbDown,
                        iconInactive = Icons.Outlined.ThumbDown,
                        text = "Dislike",
                        onClick = { viewModel.toggleDislike() }
                    )
                }
                item {
                    ActionButton(Icons.Filled.Share, "Share") { }
                }
                item {
                    ActionButton(Icons.Filled.Clipboard, "Clip") { }
                }
                item {
                    ActionButton(Icons.Filled.Download, "Download") { }
                }
                item {
                    val saveLabel = if (uiState.isInWatchLater) "Saved" else "Save"
                    ActionButton(Icons.AutoMirrored.Filled.PlaylistPlay, saveLabel) {
                        viewModel.toggleWatchLater()
                    }
                }
                item {
                    ActionButton(Icons.Filled.MoreHoriz, "") { }
                }
            }
        }

        // Channel row
        item {
            uiState.channel?.let { channel ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = channel.thumbnailUrl,
                        contentDescription = channel.title,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable { onChannelClick(channel.id) },
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = FormatUtils.formatSubscriberCount(channel.subscriberCount),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    SubscribeButton(
                        isSubscribed = uiState.isSubscribed,
                        onClick = { viewModel.toggleSubscribe() }
                    )
                }
            }
        }

        // Description
        item {
            uiState.video?.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                            .clickable { viewModel.toggleDescription() }
                    ) {
                        Text(
                            text = desc,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            maxLines = if (uiState.isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(vertical = 8.dp))

        // Comments header
        item {
            if (uiState.comments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments ${uiState.video?.commentCount?.let { FormatUtils.formatViewCount(it) } ?: ""}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Comment previews
        items(uiState.comments.take(3)) { comment ->
            CommentItem(comment)
        }

        // Related videos
        item {
            Text(
                text = "Related",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        items(uiState.relatedVideos) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video.id) },
                onChannelClick = { onChannelClick(video.channelId) },
                onMoreClick = { }
            )
        }
    }
}

@Composable
private fun LikeButton(
    isActive: Boolean,
    iconActive: androidx.compose.ui.graphics.vector.ImageVector,
    iconInactive: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) LikeBlue else TextPrimary,
        animationSpec = tween(300),
        label = "like_color"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isActive) iconActive else iconInactive,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        if (text.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
        )
        if (text.isNotEmpty()) {
            Text(text = text, color = TextPrimary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CommentItem(comment: com.youtubeclone.domain.models.Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        AsyncImage(
            model = comment.authorProfileImageUrl,
            contentDescription = comment.authorDisplayName,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorDisplayName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = FormatUtils.formatRelativeTime(comment.publishedAt),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Text(
                text = comment.textDisplay,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.ThumbUp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text(
                    text = if (comment.likeCount > 0) comment.likeCount.toString() else "",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.ThumbDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Reply", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
