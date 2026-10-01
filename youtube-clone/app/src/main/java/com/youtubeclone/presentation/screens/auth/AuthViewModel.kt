package com.youtubeclone.presentation.screens.auth

import android.app.Application
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.youtubeclone.data.local.TokenManager
import com.youtubeclone.data.repository.AuthRepository
import com.youtubeclone.domain.models.AuthResponse
import com.youtubeclone.domain.models.AuthState
import com.youtubeclone.domain.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    application: Application
) : AndroidViewModel(application) {

    private val credentialManager = CredentialManager.create(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Check if user is already authenticated
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        viewModelScope.launch {
            if (tokenManager.isAuthenticated()) {
                // Try to get fresh token
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    // Fetch user profile
                    val result = authRepository.getProfile("Bearer $token")
                    if (result.isSuccess) {
                        val profile = result.getOrNull()
                        profile?.let {
                            _uiState.value = AuthUiState.Authenticated(
                                user = authRepository.toUserDomain(profile)
                            )
                        }
                    } else {
                        // Token might be invalid, try to refresh
                        refreshTokenIfNeeded()
                    }
                } else {
                    // No token, go to idle
                    _uiState.value = AuthUiState.Idle
                }
            } else {
                _uiState.value = AuthUiState.Idle
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = (_uiState.value as? AuthUiState.Idle)?.copy(
            email = email,
            isLoading = false
        ) ?: AuthUiState.Idle(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = (_uiState.value as? AuthUiState.Idle)?.copy(
            password = password,
            isLoading = false
        ) ?: AuthUiState.Idle(password = password)
    }

    fun onShowPasswordToggle() {
        _uiState.value = (_uiState.value as? AuthUiState.Idle)?.copy(
            showPassword = !(_uiState.value as AuthUiState.Idle).showPassword
        ) ?: AuthUiState.Idle(showPassword = true)
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle()
    }

    fun login() {
        val currentState = _uiState.value
        if (currentState !is AuthUiState.Idle) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)

            val result = authRepository.login(currentState.email, currentState.password)

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
                    _uiState.value = AuthUiState.Authenticated(user)
                }
            } else {
                _uiState.value = AuthUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun googleSignIn() {
        viewModelScope.launch {
            try {
                // Show loading state
                _uiState.value = AuthUiState.Loading

                // Build the credential request
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("YOUR_GOOGLE_CLIENT_ID") // Replace with actual client ID
                    .setAssociateLinkedAccounts(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Get the credential
                val response: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = getApplication()
                )

                // Handle the credential
                handleGoogleSignInResponse(response)

            } catch (e: GetCredentialException) {
                Log.e("AuthViewModel", "Google sign in failed", e)
                _uiState.value = AuthUiState.Error(
                    message = "Google sign in failed: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign in error", e)
                _uiState.value = AuthUiState.Error(
                    message = "Error: ${e.message}"
                )
            }
        }
    }

    private suspend fun handleGoogleSignInResponse(response: GetCredentialResponse) {
        try {
            val credential = response.credential

            when (credential) {
                is GoogleIdTokenCredential -> {
                    // Extract the ID token
                    val idToken = credential.idToken

                    // Call backend to authenticate with Google
                    val result = authRepository.googleAuth(idToken)

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
                            _uiState.value = AuthUiState.Authenticated(user)
                        }
                    } else {
                        _uiState.value = AuthUiState.Error(
                            message = result.exceptionOrNull()?.message ?: "Google auth failed"
                        )
                    }
                }
                is CustomCredential -> {
                    // Handle custom credential if needed
                    _uiState.value = AuthUiState.Error(
                        message = "Custom credential not supported"
                    )
                }
                else -> {
                    _uiState.value = AuthUiState.Error(
                        message = "Unknown credential type"
                    )
                }
            }

        } catch (e: GoogleIdTokenParsingException) {
            Log.e("AuthViewModel", "Failed to parse Google ID token", e)
            _uiState.value = AuthUiState.Error(
                message = "Failed to parse Google ID token"
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error handling Google sign in", e)
            _uiState.value = AuthUiState.Error(
                message = "Error: ${e.message}"
            )
        }
    }

    private suspend fun refreshTokenIfNeeded() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken != null) {
            val result = authRepository.refreshToken(refreshToken)
            if (result.isSuccess) {
                val authResponse = result.getOrNull()
                authResponse?.let { response ->
                    // Save new tokens
                    tokenManager.saveTokens(
                        response.accessToken,
                        response.refreshToken,
                        response.expiresIn
                    )

                    // Fetch user profile
                    val profileResult = authRepository.getProfile("Bearer ${response.accessToken}")
                    if (profileResult.isSuccess) {
                        val profile = profileResult.getOrNull()
                        profile?.let {
                            _uiState.value = AuthUiState.Authenticated(
                                user = authRepository.toUserDomain(profile)
                            )
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val accessToken = tokenManager.getRawAccessToken()
            accessToken?.let {
                authRepository.logout("Bearer $it")
            }
            tokenManager.clearTokens()
            _uiState.value = AuthUiState.Unauthenticated
        }
    }
}

// UI State
sealed class AuthUiState {
    data class Idle(
        val email: String = "",
        val password: String = "",
        val showPassword: Boolean = false,
        val isLoading: Boolean = false
    ) : AuthUiState()

    object Loading : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Unauthenticated : AuthUiState()
}
