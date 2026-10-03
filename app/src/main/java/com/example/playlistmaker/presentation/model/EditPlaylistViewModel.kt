package com.example.playlistmaker.presentation.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    fun getPlaylist(
        playlistId: Long,
        onResult: (Playlist?) -> Unit
    ) {
        viewModelScope.launch {
            val playlist =
                playlistInteractor.getPlaylist(
                    playlistId
                )

            onResult(playlist)
        }
    }

    fun updatePlaylist(
        playlist: Playlist,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {

            playlistInteractor.updatePlaylist(
                playlist
            )

            onComplete()
        }
    }
}