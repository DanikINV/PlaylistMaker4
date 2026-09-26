package com.example.playlistmaker.presentation.di

import android.content.Context
import android.content.SharedPreferences
import com.example.playlistmaker.data.network.ITunesApi
import com.example.playlistmaker.data.repository.HistoryRepositoryImpl
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.data.storage.SearchHistoryStorage
import com.example.playlistmaker.data.storage.SettingsStorage
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.HistoryInteractorImpl
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractorImpl
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractorImpl
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import com.example.playlistmaker.presentation.model.PlayerViewModel
import com.example.playlistmaker.presentation.model.SearchViewModel
import com.example.playlistmaker.presentation.model.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<ITunesApi> {
        get<Retrofit>().create(ITunesApi::class.java)
    }

    single<SharedPreferences> {
        androidContext()
            .getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    }

    single {
        SearchHistoryStorage(get())
    }

    single {
        SettingsStorage(get())
    }

    single<SearchRepository> {
        SearchRepositoryImpl(get())
    }

    factory<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    single<HistoryRepository> {
        HistoryRepositoryImpl(get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<HistoryInteractor> {
        HistoryInteractorImpl(get())
    }

    factory<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    factory<PlayerInteractor> { params ->
        PlayerInteractorImpl(params.get())
    }

    viewModel {
        SearchViewModel(
            searchInteractor = get(),
            historyInteractor = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsInteractor = get()
        )
    }

    viewModel { params ->
        PlayerViewModel(
            playerInteractor = get { parametersOf(params.get<Track>()) }
        )
    }
}