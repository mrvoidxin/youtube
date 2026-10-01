package com.youtubeclone.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.youtubeclone.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    // Create or get the EncryptedSharedPreferences
    private val encryptedSharedPreferences: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            Constants.DATASTORE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ============================================
    // AUTHENTICATION TOKENS
    // ============================================

    fun saveAccessToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(Constants.KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getAccessToken(): String? {
        return encryptedSharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    fun clearAccessToken() {
        encryptedSharedPreferences.edit()
            .remove(Constants.KEY_ACCESS_TOKEN)
            .apply()
    }

    fun saveRefreshToken(token: String) {
        encryptedSharedPreferences.edit()
            .putString(Constants.KEY_REFRESH_TOKEN, token)
            .apply()
    }

    fun getRefreshToken(): String? {
        return encryptedSharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    fun clearRefreshToken() {
        encryptedSharedPreferences.edit()
            .remove(Constants.KEY_REFRESH_TOKEN)
            .apply()
    }

    fun saveTokenExpiry(expiry: Long) {
        encryptedSharedPreferences.edit()
            .putLong(Constants.KEY_TOKEN_EXPIRY, expiry)
            .apply()
    }

    fun getTokenExpiry(): Long {
        return encryptedSharedPreferences.getLong(Constants.KEY_TOKEN_EXPIRY, 0L)
    }

    fun clearTokenExpiry() {
        encryptedSharedPreferences.edit()
            .remove(Constants.KEY_TOKEN_EXPIRY)
            .apply()
    }

    fun isTokenValid(): Boolean {
        val expiry = getTokenExpiry()
        val currentTime = System.currentTimeMillis()
        return expiry > currentTime + Constants.TOKEN_REFRESH_MARGIN_MS
    }

    fun clearAuthTokens() {
        clearAccessToken()
        clearRefreshToken()
        clearTokenExpiry()
    }

    // ============================================
    // USER PREFERENCES
    // ============================================

    fun saveDarkMode(isDark: Boolean) {
        encryptedSharedPreferences.edit()
            .putBoolean(Constants.KEY_DARK_MODE, isDark)
            .apply()
    }

    fun isDarkMode(): Boolean {
        return encryptedSharedPreferences.getBoolean(Constants.KEY_DARK_MODE, true)
    }

    fun savePlaybackSpeed(speed: Float) {
        encryptedSharedPreferences.edit()
            .putFloat(Constants.KEY_PLAYBACK_SPEED, speed)
            .apply()
    }

    fun getPlaybackSpeed(): Float {
        return encryptedSharedPreferences.getFloat(Constants.KEY_PLAYBACK_SPEED, 1.0f)
    }

    // ============================================
    // SEARCH HISTORY
    // ============================================

    fun saveRecentSearches(searches: List<String>) {
        val json = gson.toJson(searches)
        encryptedSharedPreferences.edit()
            .putString(Constants.KEY_RECENT_SEARCHES, json)
            .apply()
    }

    fun getRecentSearches(): List<String> {
        val json = encryptedSharedPreferences.getString(Constants.KEY_RECENT_SEARCHES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun addRecentSearch(query: String) {
        val searches = getRecentSearches().toMutableList()
        searches.remove(query)
        searches.add(0, query)
        if (searches.size > 10) {
            searches.removeAt(searches.size - 1)
        }
        saveRecentSearches(searches)
    }

    fun clearRecentSearches() {
        encryptedSharedPreferences.edit()
            .remove(Constants.KEY_RECENT_SEARCHES)
            .apply()
    }

    // ============================================
    // CLEAR ALL
    // ============================================

    fun clearAll() {
        encryptedSharedPreferences.edit()
            .clear()
            .apply()
    }
}
