package com.example.playlistmaker.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactors.SettingsInteractor

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableLiveData(
        SettingsScreenState(
            isDarkTheme = settingsInteractor.isDarkTheme()
        )
    )

    val state: LiveData<SettingsScreenState> = _state

    fun onThemeChanged(isDark: Boolean) {
        settingsInteractor.saveDarkTheme(isDark)

        _state.value = SettingsScreenState(
            isDarkTheme = isDark
        )
    }
}