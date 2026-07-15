package com.youtubeclone.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youtubeclone.domain.models.Video
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import com.youtubeclone.presentation.theme.BgSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoOptionsSheet(
    video: Video,
    isInWatchLater: Boolean,
    onDismiss: () -> Unit,
    onSaveToWatchLater: () -> Unit,
    onSaveToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onNotInterested: () -> Unit,
    onDontRecommendChannel: () -> Unit,
    onReport: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgSecondary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = video.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = video.channelTitle,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OptionItem(
                icon = Icons.Default.WatchLater,
                text = if (isInWatchLater) "Remove from Watch Later" else "Save to Watch Later",
                onClick = onSaveToWatchLater
            )
            OptionItem(
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                text = "Save to Playlist",
                onClick = onSaveToPlaylist
            )
            OptionItem(
                icon = Icons.Default.Download,
                text = "Download",
                onClick = onDownload
            )
            OptionItem(
                icon = Icons.Default.Share,
                text = "Share",
                onClick = onShare
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OptionItem(
                icon = Icons.Default.NotInterested,
                text = "Not interested",
                onClick = onNotInterested
            )
            OptionItem(
                icon = Icons.Default.Block,
                text = "Don't recommend channel",
                onClick = onDontRecommendChannel
            )
            OptionItem(
                icon = Icons.Default.Report,
                text = "Report",
                onClick = onReport
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 15.sp
        )
    }
}
