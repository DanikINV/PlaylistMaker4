package com.example.playlistmaker.domain.interactors

interface SettingsInteractor {

    fun isDarkTheme(): Boolean

    fun saveDarkTheme(isDark: Boolean)
}