package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.presentation.di.appModule
import com.example.playlistmaker.presentation.di.dataModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class PlaylistMakerApp : Application() {


    override fun onCreate() {
        super.onCreate()


        startKoin {

            androidContext(this@PlaylistMakerApp)

            modules(
                appModule,
                dataModule
            )
        }

        val settingsInteractor =
            getKoin().get<SettingsInteractor>()

        AppCompatDelegate.setDefaultNightMode(

            if (settingsInteractor.isDarkTheme()) {

                AppCompatDelegate.MODE_NIGHT_YES

            } else {

                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}