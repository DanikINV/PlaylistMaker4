package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.storage.SettingsStorage
import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val storage: SettingsStorage
) : SettingsRepository {

    override fun isDarkTheme(): Boolean {
        return storage.isDarkTheme()
    }

    override fun saveDarkTheme(isDark: Boolean) {
        storage.saveDarkTheme(isDark)
    }
}