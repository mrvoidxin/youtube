package com.youtubeclone.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubeclone.data.local.PreferencesManager
import com.youtubeclone.presentation.theme.AccentRed
import com.youtubeclone.presentation.theme.BgPrimary
import com.youtubeclone.presentation.theme.TextPrimary
import com.youtubeclone.presentation.theme.TextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    val isDarkMode = preferencesManager.isDarkMode
    val isAutoplayEnabled = preferencesManager.isAutoplayEnabled
    val isCaptionsEnabled = preferencesManager.isCaptionsEnabled
    val isRestrictedMode = preferencesManager.isRestrictedMode
    val isDataSaverEnabled = preferencesManager.isDataSaverEnabled
    val isNotificationsEnabled = preferencesManager.isNotificationsEnabled

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDarkMode(enabled) }
    }

    fun setAutoplay(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAutoplay(enabled) }
    }

    fun setCaptions(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setCaptionsEnabled(enabled) }
    }

    fun setRestrictedMode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setRestrictedMode(enabled) }
    }

    fun setDataSaver(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDataSaver(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotificationsEnabled(enabled) }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = true)
    val isAutoplay by viewModel.isAutoplayEnabled.collectAsState(initial = true)
    val isCaptions by viewModel.isCaptionsEnabled.collectAsState(initial = false)
    val isRestricted by viewModel.isRestrictedMode.collectAsState(initial = false)
    val isDataSaver by viewModel.isDataSaverEnabled.collectAsState(initial = false)
    val isNotifications by viewModel.isNotificationsEnabled.collectAsState(initial = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Account Section
            item {
                SectionTitle("Account")
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Brightness6,
                    title = "Appearance",
                    subtitle = if (isDarkMode) "Dark theme" else "Light theme",
                    onClick = { }
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.Brightness6,
                    title = "Dark Mode",
                    subtitle = "Toggle dark theme",
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            // Video Settings
            item {
                SectionTitle("Video")
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.PlayCircle,
                    title = "Autoplay",
                    subtitle = "Autoplay next video",
                    checked = isAutoplay,
                    onCheckedChange = { viewModel.setAutoplay(it) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Hd,
                    title = "Video quality preferences",
                    subtitle = "Auto",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Speed,
                    title = "Playback speed",
                    subtitle = "Normal",
                    onClick = { }
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.ClosedCaption,
                    title = "Captions",
                    subtitle = "Show captions when available",
                    checked = isCaptions,
                    onCheckedChange = { viewModel.setCaptions(it) }
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            // General
            item {
                SectionTitle("General")
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    subtitle = "Receive push notifications",
                    checked = isNotifications,
                    onCheckedChange = { viewModel.setNotifications(it) }
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.Visibility,
                    title = "Restricted Mode",
                    subtitle = "Filter potentially mature content",
                    checked = isRestricted,
                    onCheckedChange = { viewModel.setRestrictedMode(it) }
                )
            }
            item {
                SwitchSettingsItem(
                    icon = Icons.Filled.DataSaverOn,
                    title = "Data Saver",
                    subtitle = "Reduce data usage on mobile",
                    checked = isDataSaver,
                    onCheckedChange = { viewModel.setDataSaver(it) }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.GTranslate,
                    title = "Language",
                    subtitle = "English",
                    onClick = { }
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            // Privacy & Safety
            item {
                SectionTitle("Privacy & Safety")
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy",
                    subtitle = "Manage your data",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Security",
                    subtitle = "Account security settings",
                    onClick = { }
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }

            // About
            item {
                SectionTitle("About")
            }
            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About YouTube Clone",
                    subtitle = "Version 1.0.0",
                    onClick = { }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = "Help & Feedback",
                    subtitle = "",
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 15.sp)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SwitchSettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 15.sp)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, color = TextSecondary, fontSize = 13.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentRed,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
