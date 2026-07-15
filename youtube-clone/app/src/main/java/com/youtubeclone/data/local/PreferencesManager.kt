package com.youtubeclone.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.youtubeclone.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_PLAYBACK_SPEED = stringPreferencesKey("playback_speed")
        private val KEY_PLAYBACK_QUALITY = stringPreferencesKey("playback_quality")
        private val KEY_AUTOPLAY = booleanPreferencesKey("autoplay")
        private val KEY_CAPTIONS_ENABLED = booleanPreferencesKey("captions_enabled")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_RESTRICTED_MODE = booleanPreferencesKey("restricted_mode")
        private val KEY_DATA_SAVER = booleanPreferencesKey("data_saver")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true
    }

    val playbackSpeed: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_PLAYBACK_SPEED] ?: "1.0"
    }

    val playbackQuality: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_PLAYBACK_QUALITY] ?: "auto"
    }

    val isAutoplayEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTOPLAY] ?: true
    }

    val isCaptionsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_CAPTIONS_ENABLED] ?: false
    }

    val isNotificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val isRestrictedMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_RESTRICTED_MODE] ?: false
    }

    val isDataSaverEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DATA_SAVER] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun setPlaybackSpeed(speed: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PLAYBACK_SPEED] = speed
        }
    }

    suspend fun setPlaybackQuality(quality: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PLAYBACK_QUALITY] = quality
        }
    }

    suspend fun setAutoplay(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTOPLAY] = enabled
        }
    }

    suspend fun setCaptionsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_CAPTIONS_ENABLED] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setRestrictedMode(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_RESTRICTED_MODE] = enabled
        }
    }

    suspend fun setDataSaver(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DATA_SAVER] = enabled
        }
    }
}
