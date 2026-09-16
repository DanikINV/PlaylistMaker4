package com.example.playlistmaker.data.storage

import android.content.SharedPreferences

class SettingsStorage(
    private val prefs: SharedPreferences
) {

    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(
            DARK_THEME_KEY,
            false
        )
    }

    fun saveDarkTheme(isDark: Boolean) {
        prefs.edit()
            .putBoolean(
                DARK_THEME_KEY,
                isDark
            )
            .apply()
    }

    companion object {
        private const val DARK_THEME_KEY = "dark_theme"
    }
}