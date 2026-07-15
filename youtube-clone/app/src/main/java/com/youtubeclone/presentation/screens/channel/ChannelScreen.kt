package com.youtubeclone.presentation.screens.channel

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.youtubeclone.presentation.components.SubscribeButton
import com.youtubeclone.presentation.theme.AccentRed
import com.youtubeclone.presentation.theme.BgPrimary
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import com.youtubeclone.utils.Constants
import com.youtubeclone.utils.FormatUtils

@Composable
fun ChannelScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize().background(BgPrimary), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed)
            }
        }

        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize().background(BgPrimary), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.error ?: "Error", color = TextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Tap to go back", color = AccentRed, modifier = Modifier.clickable { onBack() })
                }
            }
        }

        else -> {
            val channel = uiState.channel ?: return

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgPrimary)
            ) {
                // Banner + avatar
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        // Banner
                        if (channel.bannerUrl.isNotEmpty()) {
                            AsyncImage(
                                model = channel.bannerUrl,
                                contentDescription = "Channel banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2D2D2D))
                            )
                        }

                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                tint = Color.White
                            )
                        }

                        // Avatar overlapping
                        AsyncImage(
                            model = channel.thumbnailUrl,
                            contentDescription = channel.title,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Channel info
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = channel.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            Text(
                                text = "@${channel.customUrl.ifEmpty { channel.title }}",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " · ${FormatUtils.formatSubscriberCount(channel.subscriberCount)}",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = " · ${channel.videoCount} videos",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        if (channel.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = channel.description,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Buttons row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubscribeButton(
                                isSubscribed = uiState.isSubscribed,
                                onClick = { viewModel.toggleSubscribe() }
                            )

                            IconButton(
                                onClick = { viewModel.toggleBell() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    "Bell",
                                    tint = if (uiState.isBellEnabled) AccentRed else TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    "More",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Tabs
                item {
                    val tabs = Constants.CHANNEL_TABS
                    val selectedIndex = uiState.selectedTabIndex

                    TabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = BgPrimary,
                        contentColor = TextPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = TextPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = index == selectedIndex,
                                onClick = { viewModel.selectTab(index) },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (index == selectedIndex) TextPrimary else TextSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // Content based on selected tab
                when (uiState.selectedTabIndex) {
                    0, 1 -> {
                        // Videos grid
                        items(uiState.videos.chunked(2)) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { video ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onVideoClick(video.id) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = video.thumbnailUrl,
                                                contentDescription = video.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            if (video.duration.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = FormatUtils.formatDuration(video.duration),
                                                        color = Color.White,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = video.title,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "${FormatUtils.formatViewCount(video.viewCount.toLong())} · ${FormatUtils.formatRelativeTime(video.publishedAt)}",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Fill empty space if odd count
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
