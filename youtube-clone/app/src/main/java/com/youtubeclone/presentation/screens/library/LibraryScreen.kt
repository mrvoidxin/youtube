package com.youtubeclone.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.youtubeclone.data.local.entities.HistoryEntity
import com.youtubeclone.data.local.entities.LikedVideoEntity
import com.youtubeclone.data.local.entities.WatchLaterEntity
import com.youtubeclone.data.repository.LocalRepository
import com.youtubeclone.presentation.theme.BgPrimary
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import com.youtubeclone.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : androidx.lifecycle.ViewModel() {
    val watchHistory = localRepository.getWatchHistory()
    val watchLater = localRepository.getWatchLater()
    val likedVideos = localRepository.getLikedVideos()
}

@Composable
fun LibraryScreen(
    onHistoryClick: () -> Unit,
    onWatchLaterClick: () -> Unit,
    onLikedVideosClick: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val watchLater by viewModel.watchLater.collectAsStateWithLifecycle(initialValue = emptyList())
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle(initialValue = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(top = 16.dp)
    ) {
        // History section
        if (watchHistory.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.History,
                    title = "History",
                    onClick = onHistoryClick
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistory.take(10)) { history ->
                        HorizontalVideoItem(
                            title = history.title,
                            thumbnail = history.thumbnail,
                            channelName = history.channelName,
                            onClick = { onVideoClick(history.videoId) }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Watch Later section
        if (watchLater.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.WatchLater,
                    title = "Watch Later",
                    onClick = onWatchLaterClick
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchLater.take(10)) { item ->
                        HorizontalVideoItem(
                            title = item.title,
                            thumbnail = item.thumbnail,
                            channelName = item.channelName,
                            onClick = { onVideoClick(item.videoId) }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Liked Videos section
        if (likedVideos.isNotEmpty()) {
            item {
                SectionHeader(
                    icon = Icons.Filled.ThumbUp,
                    title = "Liked Videos",
                    onClick = onLikedVideosClick
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(likedVideos.take(10)) { item ->
                        HorizontalVideoItem(
                            title = item.title,
                            thumbnail = item.thumbnail,
                            channelName = item.channelName,
                            onClick = { onVideoClick(item.videoId) }
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Downloads placeholder
        item {
            SectionHeader(
                icon = Icons.Filled.Download,
                title = "Downloads",
                onClick = { }
            )
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No downloads yet",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = TextPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(text = "See all", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun HorizontalVideoItem(
    title: String,
    thumbnail: String,
    channelName: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = thumbnail,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = channelName,
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
