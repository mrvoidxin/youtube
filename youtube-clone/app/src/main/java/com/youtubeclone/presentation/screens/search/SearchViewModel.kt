package com.youtubeclone.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.repository.SearchRepository
import com.youtubeclone.domain.models.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val suggestions: List<Video> = emptyList(),
    val searchResults: List<Video> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val nextPageToken: String? = null,
    val selectedFilterIndex: Int = 0,
    val isSearchActive: Boolean = false,
    val showSuggestions: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var suggestionJob: Job? = null

    private val recentSearches = mutableListOf<String>()

    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                showSuggestions = query.isNotEmpty(),
                isSearchActive = query.isNotEmpty()
            )
        }

        suggestionJob?.cancel()
        if (query.length >= 2) {
            suggestionJob = viewModelScope.launch {
                delay(300) // debounce
                loadSuggestions(query)
            }
        }
    }

    private suspend fun loadSuggestions(query: String) {
        val result = searchRepository.searchVideos(query)
        result.fold(
            onSuccess = { searchResult ->
                _uiState.update {
                    it.copy(suggestions = searchResult.videos.take(5))
                }
            },
            onFailure = { /* ignore suggestion errors */ }
        )
    }

    fun performSearch(query: String = _uiState.value.query) {
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    showSuggestions = false,
                    isSearchActive = true
                )
            }

            // Add to recent searches
            if (!recentSearches.contains(query)) {
                recentSearches.add(0, query)
                if (recentSearches.size > 20) {
                    recentSearches.removeAt(recentSearches.lastIndex)
                }
            }
            _uiState.update { it.copy(recentSearches = recentSearches.toList()) }

            val filterType = when (_uiState.value.selectedFilterIndex) {
                2 -> "channel"
                3 -> "playlist"
                else -> "video"
            }

            val result = searchRepository.searchVideos(
                query = query,
                type = if (filterType == "video") filterType else filterType
            )

            result.fold(
                onSuccess = { searchResult ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            searchResults = searchResult.videos,
                            nextPageToken = searchResult.nextPageToken,
                            query = query
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Search failed"
                        )
                    }
                }
            )
        }
    }

    fun loadMoreResults() {
        if (_uiState.value.isLoadingMore || _uiState.value.nextPageToken == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val result = searchRepository.searchVideos(
                query = _uiState.value.query,
                pageToken = _uiState.value.nextPageToken
            )

            result.fold(
                onSuccess = { searchResult ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingMore = false,
                            searchResults = state.searchResults + searchResult.videos,
                            nextPageToken = searchResult.nextPageToken
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            )
        }
    }

    fun selectFilter(index: Int) {
        _uiState.update { it.copy(selectedFilterIndex = index) }
        if (_uiState.value.isSearchActive) {
            performSearch()
        }
    }

    fun removeRecentSearch(search: String) {
        recentSearches.remove(search)
        _uiState.update { it.copy(recentSearches = recentSearches.toList()) }
    }

    fun clearSearch() {
        searchJob?.cancel()
        suggestionJob?.cancel()
        _uiState.update {
            SearchUiState(recentSearches = recentSearches.toList())
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
