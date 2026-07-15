package com.youtubeclone.presentation.screens.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.repository.VideoRepository
import com.youtubeclone.domain.models.Channel
import com.youtubeclone.domain.models.Video
import com.youtubeclone.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelUiState(
    val channel: Channel? = null,
    val videos: List<Video> = emptyList(),
    val shorts: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val isSubscribed: Boolean = false,
    val isBellEnabled: Boolean = false,
    val nextPageToken: String? = null
)

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val channelId: String = savedStateHandle.get<String>("channelId") ?: ""

    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()

    init {
        loadChannel()
    }

    private fun loadChannel() {
        if (channelId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val channelResult = videoRepository.getChannelDetails(channelId)

            channelResult.fold(
                onSuccess = { channel ->
                    _uiState.update { it.copy(channel = channel, isLoading = false) }
                    loadChannelVideos()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load channel"
                        )
                    }
                }
            )
        }
    }

    private fun loadChannelVideos(pageToken: String? = null) {
        viewModelScope.launch {
            val result = videoRepository.getTrendingVideos(pageToken = pageToken)

            result.fold(
                onSuccess = { trendingResult ->
                    val channelVideos = trendingResult.videos.filter {
                        it.channelId == channelId
                    }
                    _uiState.update { state ->
                        state.copy(
                            videos = if (pageToken == null) channelVideos
                            else state.videos + channelVideos,
                            nextPageToken = trendingResult.nextPageToken
                        )
                    }
                },
                onFailure = { /* ignore */ }
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun toggleSubscribe() {
        _uiState.update { it.copy(isSubscribed = !it.isSubscribed) }
    }

    fun toggleBell() {
        _uiState.update { it.copy(isBellEnabled = !it.isBellEnabled) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
