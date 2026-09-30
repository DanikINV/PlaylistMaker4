package com.example.playlistmaker.presentation.di

import androidx.room.Room
import com.example.playlistmaker.data.db.FavoriteTrackDao
import com.example.playlistmaker.data.db.FavoriteTracksDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            FavoriteTracksDatabase::class.java,
            "playlist_maker.db"
        ).build()
    }

    single<FavoriteTrackDao> {
        get<FavoriteTracksDatabase>().favoriteTrackDao()
    }
}