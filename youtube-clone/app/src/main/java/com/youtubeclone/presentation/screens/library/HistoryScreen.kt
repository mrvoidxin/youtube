package com.youtubeclone.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.local.entities.HistoryEntity
import com.youtubeclone.data.repository.LocalRepository
import kotlinx.coroutines.launch
import com.youtubeclone.presentation.theme.AccentRed
import com.youtubeclone.presentation.theme.BgPrimary
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import com.youtubeclone.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : androidx.lifecycle.ViewModel() {
    val watchHistory = localRepository.getWatchHistory()

    fun clearAll() {
        viewModelScope.launch {
            localRepository.clearHistory()
        }
    }

    fun removeItem(videoId: String) {
        viewModelScope.launch {
            localRepository.removeFromHistory(videoId)
        }
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Group by date
    val grouped = remember(watchHistory) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000))

        watchHistory.groupBy { item ->
            val date = sdf.format(Date(item.watchedAt))
            when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val displaySdf = SimpleDateFormat("MMM d", Locale.US)
                    displaySdf.format(Date(item.watchedAt))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                text = "History",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, "Search", tint = TextPrimary)
            }
            IconButton(onClick = { showClearDialog = true }) {
                Icon(Icons.Filled.ClearAll, "Clear all", tint = TextPrimary)
            }
        }

        if (watchHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No watch history", color = TextSecondary, fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (dateLabel, items) ->
                    item {
                        Text(
                            text = dateLabel,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    items(items, key = { it.videoId }) { history ->
                        HistoryItem(
                            history = history,
                            onClick = { onVideoClick(history.videoId) },
                            onRemove = { viewModel.removeItem(history.videoId) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear watch history?", color = TextPrimary) },
            text = { Text("This will remove all videos from your watch history.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Clear", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BgPrimary
        )
    }
}

@Composable
private fun HistoryItem(
    history: HistoryEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = history.thumbnail,
            contentDescription = history.title,
            modifier = Modifier
                .size(120.dp, 68.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = history.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${history.channelName} · ${FormatUtils.formatViewCount(history.viewCount.toLong())}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.MoreVert, "Remove", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}
