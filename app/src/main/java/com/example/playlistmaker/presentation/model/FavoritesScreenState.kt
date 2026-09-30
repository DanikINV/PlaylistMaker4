package com.example.playlistmaker.presentation.model

import com.example.playlistmaker.domain.model.Track

sealed interface FavoritesScreenState {

    data object Empty : FavoritesScreenState

    data class Content(
        val tracks: List<Track>
    ) : FavoritesScreenState
}