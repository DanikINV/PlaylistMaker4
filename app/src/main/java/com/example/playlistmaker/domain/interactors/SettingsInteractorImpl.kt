package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun isDarkTheme(): Boolean {
        return repository.isDarkTheme()
    }

    override fun saveDarkTheme(isDark: Boolean) {
        repository.saveDarkTheme(isDark)
    }
}