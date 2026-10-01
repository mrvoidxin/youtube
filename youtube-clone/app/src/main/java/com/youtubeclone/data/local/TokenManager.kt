package com.youtubeclone.data.local

import com.youtubeclone.data.repository.AuthRepository
import com.youtubeclone.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager handles JWT token storage, retrieval, and automatic refresh.
 * Uses EncryptedSharedPreferences for secure token storage.
 */
@Singleton
class TokenManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) {

    /**
     * Save authentication tokens
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Int) {
        withContext(Dispatchers.IO) {
            preferencesManager.saveAccessToken(accessToken)
            preferencesManager.saveRefreshToken(refreshToken)
            // Calculate expiry time (current time + expiresIn seconds)
            val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L)
            preferencesManager.saveTokenExpiry(expiryTime)
        }
    }

    /**
     * Get access token, refreshing if necessary
     */
    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            val currentToken = preferencesManager.getAccessToken()
            val currentExpiry = preferencesManager.getTokenExpiry()
            val currentTime = System.currentTimeMillis()

            // Check if token is valid (has margin for refresh)
            if (currentToken != null && currentExpiry > currentTime + Constants.TOKEN_REFRESH_MARGIN_MS) {
                return@withContext currentToken
            }

            // Try to refresh token
            val refreshToken = preferencesManager.getRefreshToken()
            if (refreshToken != null) {
                val result = authRepository.refreshToken(refreshToken)
                if (result.isSuccess) {
                    val authResponse = result.getOrNull()
                    authResponse?.let {
                        saveTokens(
                            it.accessToken,
                            it.refreshToken,
                            it.expiresIn
                        )
                        return@withContext it.accessToken
                    }
                }
            }

            // Token refresh failed, clear tokens
            clearTokens()
            null
        }
    }

    /**
     * Get raw access token without auto-refresh
     */
    fun getRawAccessToken(): String? {
        return preferencesManager.getAccessToken()
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return preferencesManager.getRefreshToken()
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return preferencesManager.getAccessToken() != null
    }

    /**
     * Clear all tokens
     */
    fun clearTokens() {
        preferencesManager.clearAuthTokens()
    }

    /**
     * Check if token is about to expire
     */
    fun isTokenAboutToExpire(): Boolean {
        val expiry = preferencesManager.getTokenExpiry()
        val currentTime = System.currentTimeMillis()
        return expiry <= currentTime + Constants.TOKEN_REFRESH_MARGIN_MS
    }
}
