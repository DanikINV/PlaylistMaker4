package com.example.playlistmaker.domain.repository

interface SettingsRepository {

    fun isDarkTheme(): Boolean

    fun saveDarkTheme(isDark: Boolean)
}