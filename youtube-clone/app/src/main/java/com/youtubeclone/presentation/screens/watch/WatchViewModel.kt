package com.youtubeclone.presentation.screens.watch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.repository.LocalRepository
import com.youtubeclone.data.repository.VideoRepository
import com.youtubeclone.domain.models.Channel
import com.youtubeclone.domain.models.Comment
import com.youtubeclone.domain.models.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchUiState(
    val video: Video? = null,
    val channel: Channel? = null,
    val comments: List<Comment> = emptyList(),
    val relatedVideos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isInWatchLater: Boolean = false,
    val isSubscribed: Boolean = false,
    val commentsNextPageToken: String? = null,
    val isLoadingComments: Boolean = false,
    val isDescriptionExpanded: Boolean = false,
    val playerState: PlayerState = PlayerState.IDLE
)

enum class PlayerState {
    IDLE, PLAYING, PAUSED, BUFFERING, ENDED
}

@HiltViewModel
class WatchViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val localRepository: LocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val videoId: String = savedStateHandle.get<String>("videoId") ?: ""

    private val _uiState = MutableStateFlow(WatchUiState())
    val uiState: StateFlow<WatchUiState> = _uiState.asStateFlow()

    init {
        loadVideoDetails()
    }

    private fun loadVideoDetails() {
        if (videoId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val videoResult = videoRepository.getVideoDetails(videoId)
            videoResult.fold(
                onSuccess = { video ->
                    _uiState.update { it.copy(video = video, isLoading = false) }
                    // Add to history
                    localRepository.addToHistory(video)

                    // Load channel details
                    loadChannelDetails(video.channelId)

                    // Load comments
                    loadComments()

                    // Load related videos
                    loadRelatedVideos()

                    // Check local state
                    checkLocalState()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Failed to load video")
                    }
                }
            )
        }
    }

    private fun loadChannelDetails(channelId: String) {
        viewModelScope.launch {
            val result = videoRepository.getChannelDetails(channelId)
            result.fold(
                onSuccess = { channel ->
                    _uiState.update { it.copy(channel = channel) }
                },
                onFailure = { /* silently fail */ }
            )
        }
    }

    private fun loadComments(pageToken: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }

            val result = videoRepository.getVideoComments(videoId, pageToken)
            result.fold(
                onSuccess = { commentsResult ->
                    _uiState.update { state ->
                        state.copy(
                            comments = if (pageToken == null) commentsResult.comments
                            else state.comments + commentsResult.comments,
                            commentsNextPageToken = commentsResult.nextPageToken,
                            isLoadingComments = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingComments = false) }
                }
            )
        }
    }

    private fun loadRelatedVideos(pageToken: String? = null) {
        viewModelScope.launch {
            val result = videoRepository.getRelatedVideos(videoId, pageToken)
            result.fold(
                onSuccess = { relatedResult ->
                    _uiState.update { state ->
                        state.copy(
                            relatedVideos = if (pageToken == null) relatedResult.videos
                            else state.relatedVideos + relatedResult.videos
                        )
                    }
                },
                onFailure = { /* silently fail */ }
            )
        }
    }

    private suspend fun checkLocalState() {
        val isLiked = localRepository.isLiked(videoId)
        val isInWatchLater = localRepository.isInWatchLater(videoId)
        _uiState.update {
            it.copy(isLiked = isLiked, isInWatchLater = isInWatchLater)
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val video = _uiState.value.video ?: return@launch
            val isLiked = !_uiState.value.isLiked
            if (isLiked) {
                localRepository.addToLikedVideos(video)
            } else {
                localRepository.removeFromLikedVideos(video.id)
            }
            _uiState.update {
                it.copy(isLiked = isLiked, isDisliked = false)
            }
        }
    }

    fun toggleDislike() {
        viewModelScope.launch {
            val isDisliked = !_uiState.value.isDisliked
            if (_uiState.value.isLiked) {
                localRepository.removeFromLikedVideos(_uiState.value.video?.id.orEmpty())
            }
            _uiState.update {
                it.copy(isDisliked = isDisliked, isLiked = false)
            }
        }
    }

    fun toggleWatchLater() {
        viewModelScope.launch {
            val video = _uiState.value.video ?: return@launch
            val isInWatchLater = !_uiState.value.isInWatchLater
            if (isInWatchLater) {
                localRepository.addToWatchLater(video)
            } else {
                localRepository.removeFromWatchLater(video.id)
            }
            _uiState.update { it.copy(isInWatchLater = isInWatchLater) }
        }
    }

    fun toggleDescription() {
        _uiState.update { it.copy(isDescriptionExpanded = !it.isDescriptionExpanded) }
    }

    fun setPlayerState(state: PlayerState) {
        _uiState.update { it.copy(playerState = state) }
    }

    fun toggleSubscribe() {
        _uiState.update { it.copy(isSubscribed = !it.isSubscribed) }
    }

    fun loadMoreComments() {
        val token = _uiState.value.commentsNextPageToken ?: return
        loadComments(token)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
