package com.youtubeclone.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun checkAuthStatus() {
        viewModelScope.launch {
            // Simulate a small delay to show splash screen
            // In production, you might want to check auth status asynchronously
            if (tokenManager.isAuthenticated()) {
                _uiState.value = SplashUiState.Authenticated
            } else {
                _uiState.value = SplashUiState.Unauthenticated
            }
        }
    }
}

sealed class SplashUiState {
    object Loading : SplashUiState()
    object Authenticated : SplashUiState()
    object Unauthenticated : SplashUiState()
}
