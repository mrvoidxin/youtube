package com.youtubeclone.presentation.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.local.TokenManager
import com.youtubeclone.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = (_uiState.value as? RegisterUiState.Idle)?.copy(
            email = email,
            isLoading = false
        ) ?: RegisterUiState.Idle(email = email)
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.value = (_uiState.value as? RegisterUiState.Idle)?.copy(
            displayName = displayName,
            isLoading = false
        ) ?: RegisterUiState.Idle(displayName = displayName)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = (_uiState.value as? RegisterUiState.Idle)?.copy(
            password = password,
            isLoading = false
        ) ?: RegisterUiState.Idle(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = (_uiState.value as? RegisterUiState.Idle)?.copy(
            confirmPassword = confirmPassword,
            isLoading = false
        ) ?: RegisterUiState.Idle(confirmPassword = confirmPassword)
    }

    fun onShowPasswordToggle() {
        _uiState.value = (_uiState.value as? RegisterUiState.Idle)?.copy(
            showPassword = !(_uiState.value as RegisterUiState.Idle).showPassword
        ) ?: RegisterUiState.Idle(showPassword = true)
    }

    fun clearError() {
        _uiState.value = RegisterUiState.Idle()
    }

    fun register() {
        val currentState = _uiState.value
        if (currentState !is RegisterUiState.Idle) return

        // Validate inputs
        if (currentState.email.isBlank() || currentState.password.isBlank() || currentState.displayName.isBlank()) {
            _uiState.value = RegisterUiState.Error("Please fill all fields")
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = RegisterUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val result = authRepository.register(
                currentState.email,
                currentState.password,
                currentState.displayName
            )

            if (result.isSuccess) {
                val authResponse = result.getOrNull()
                authResponse?.let { response ->
                    // Save tokens
                    tokenManager.saveTokens(
                        response.accessToken,
                        response.refreshToken,
                        response.expiresIn
                    )

                    // Convert to user domain
                    val user = authRepository.toUserDomain(response.user)

                    // Update state
                    _uiState.value = RegisterUiState.Authenticated(user)
                }
            } else {
                _uiState.value = RegisterUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }
}

// UI State
sealed class RegisterUiState {
    data class Idle(
        val email: String = "",
        val displayName: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val showPassword: Boolean = false,
        val isLoading: Boolean = false
    ) : RegisterUiState()

    object Loading : RegisterUiState()
    data class Authenticated(val user: com.youtubeclone.domain.models.User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
