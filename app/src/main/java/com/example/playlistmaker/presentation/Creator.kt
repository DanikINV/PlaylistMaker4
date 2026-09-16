package com.example.playlistmaker.presentation

import android.content.Context
import com.example.playlistmaker.data.network.ITunesNetworkClient
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.storage.SearchHistoryStorage
import com.example.playlistmaker.data.storage.SettingsStorage
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractorImpl
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractorImpl

object Creator {

    private const val PREFS_NAME = "playlist_maker_prefs"

    fun provideSearchInteractor(): SearchInteractor {
        val repository = SearchRepositoryImpl(
            ITunesNetworkClient.service
        )

        return SearchInteractorImpl(repository)
    }

    fun provideHistoryInteractor(
        context: Context
    ): HistoryInteractor {

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val storage = SearchHistoryStorage(prefs)

        val repository = HistoryRepositoryImpl(
            storage
        )

        return HistoryInteractorImpl(repository)
    }

    fun provideSettingsInteractor(
        context: Context
    ): SettingsInteractor {

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val storage = SettingsStorage(prefs)

        val repository = SettingsRepositoryImpl(
            storage
        )

        return SettingsInteractorImpl(repository)
    }
}