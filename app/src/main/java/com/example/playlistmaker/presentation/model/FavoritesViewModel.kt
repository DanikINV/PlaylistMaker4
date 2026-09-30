package com.example.playlistmaker.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.playlistmaker.domain.interactors.FavoriteTrackInteractor
import com.example.playlistmaker.domain.model.Track

class FavoritesViewModel(
    private val favoriteTrackInteractor: FavoriteTrackInteractor
) : ViewModel() {

    val favoriteTracks: LiveData<List<Track>> =
        favoriteTrackInteractor
            .getFavoriteTracks()
            .asLiveData()
}