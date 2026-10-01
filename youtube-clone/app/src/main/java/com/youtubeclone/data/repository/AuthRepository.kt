package com.youtubeclone.data.repository

import com.youtubeclone.data.api.BackendApiService
import com.youtubeclone.data.models.*
import com.youtubeclone.domain.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: BackendApiService
) {

    suspend fun register(email: String, password: String, displayName: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(email, password, displayName)
            val response = apiService.register(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleAuth(credential: String): Result<AuthResponse> {
        return try {
            val request = GoogleAuthRequest(credential)
            val response = apiService.googleAuth(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Google auth failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(refreshToken: String): Result<AuthResponse> {
        return try {
            val request = RefreshTokenRequest(refreshToken)
            val response = apiService.refreshToken(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Refresh token failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(accessToken: String): Result<Unit> {
        return try {
            val response = apiService.logout("Bearer $accessToken")
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(accessToken: String): Result<UserProfileResponse> {
        return try {
            val response = apiService.getProfile("Bearer $accessToken")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("Get profile failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to convert UserProfileResponse to User domain model
    fun toUserDomain(userProfile: UserProfileResponse): User {
        return User(
            id = userProfile.id,
            email = userProfile.email,
            displayName = userProfile.displayName,
            avatarUrl = userProfile.avatarUrl,
            createdAt = userProfile.createdAt
        )
    }
}
