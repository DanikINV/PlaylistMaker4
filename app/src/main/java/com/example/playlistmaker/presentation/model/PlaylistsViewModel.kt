package com.example.playlistmaker.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    val playlists: LiveData<List<Playlist>> =
        playlistInteractor.getPlaylists().asLiveData()
}