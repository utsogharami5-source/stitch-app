package com.smartbudge.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smartbudge_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true)) // Default to dark
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(prefs.getLong("last_backup_time", 0L))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setLastBackupTime(time: Long) {
        prefs.edit().putLong("last_backup_time", time).apply()
        _lastBackupTime.value = time
    }
}
