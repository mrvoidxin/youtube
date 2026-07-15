package com.youtubeclone.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.repository.VideoRepository
import com.youtubeclone.domain.models.Video
import com.youtubeclone.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedCategoryIndex: Int = 0,
    val selectedCategoryId: String? = null,
    val nextPageToken: String? = null,
    val hasMorePages: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTrendingVideos()
    }

    fun loadTrendingVideos(refresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                if (refresh) {
                    it.copy(isRefreshing = true, error = null)
                } else {
                    it.copy(isLoading = true, error = null)
                }
            }

            val token = if (refresh) null else _uiState.value.nextPageToken
            val categoryId = _uiState.value.selectedCategoryId

            val result = videoRepository.getTrendingVideos(
                pageToken = token,
                categoryId = categoryId
            )

            result.fold(
                onSuccess = { trendingResult ->
                    _uiState.update { state ->
                        state.copy(
                            videos = if (refresh || token == null) trendingResult.videos
                            else state.videos + trendingResult.videos,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                            nextPageToken = trendingResult.nextPageToken,
                            hasMorePages = trendingResult.nextPageToken != null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "Failed to load videos"
                        )
                    }
                }
            )
        }
    }

    fun selectCategory(index: Int) {
        val category = Constants.CATEGORIES.getOrNull(index) ?: return
        _uiState.update {
            it.copy(
                selectedCategoryIndex = index,
                selectedCategoryId = if (category.second == Constants.HOME_CATEGORY_ALL) null
                else category.second
            )
        }
        loadTrendingVideos(refresh = true)
    }

    fun refresh() {
        loadTrendingVideos(refresh = true)
    }

    fun loadMore() {
        if (!_uiState.value.isLoading && _uiState.value.hasMorePages) {
            loadTrendingVideos()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
