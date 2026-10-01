package com.youtubeclone.domain.models

/**
 * Authentication state for the application
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

/**
 * User domain model
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String? = null
)

/**
 * Login request model
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Register request model
 */
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

/**
 * Authentication response model
 */
data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)
