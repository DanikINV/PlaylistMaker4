package com.example.playlistmaker.presentation.di

import android.content.SharedPreferences
import androidx.room.Room
import com.example.playlistmaker.data.db.FavoriteTrackDao
import com.example.playlistmaker.data.db.FavoriteTracksDatabase
import com.example.playlistmaker.data.db.PlaylistTrackDao
import com.example.playlistmaker.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.domain.interactors.SettingsInteractorImpl
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.example.playlistmaker.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FavoriteTracksDatabase::class.java,
            "playlist_maker.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single<FavoriteTrackDao> {
        get<FavoriteTracksDatabase>().favoriteTrackDao()
    }

    single {
        get<FavoriteTracksDatabase>().playlistDao()
    }

    single<PlaylistTrackDao> {
        get<FavoriteTracksDatabase>().playlistTrackDao()
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            playlistTrackDao = get()
        )
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            "settings",
            android.content.Context.MODE_PRIVATE
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
}