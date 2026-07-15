package com.youtubeclone.presentation.screens.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.repository.SearchRepository
import com.youtubeclone.domain.models.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShortsUiState(
    val shorts: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true
)

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShortsUiState())
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    init {
        loadShorts()
    }

    fun loadShorts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = searchRepository.searchShorts(query = "shorts")

            result.fold(
                onSuccess = { searchResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            shorts = searchResult.videos,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load shorts"
                        )
                    }
                }
            )
        }
    }

    fun setCurrentIndex(index: Int) {
        _uiState.update { it.copy(currentIndex = index) }
    }

    fun togglePlayback() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
