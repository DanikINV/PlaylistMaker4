package com.example.playlistmaker.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlaylistScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val tracksCount: Int = 0,
    val duration: String = "0"
)

class PlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state =
        MutableLiveData(
            PlaylistScreenState()
        )

    val state: LiveData<PlaylistScreenState> =
        _state

    private val _playlistDeleted =
        MutableLiveData(false)

    val playlistDeleted: LiveData<Boolean> =
        _playlistDeleted

    fun loadPlaylist(
        playlistId: Long
    ) {
        viewModelScope.launch {

            val playlist =
                playlistInteractor.getPlaylist(
                    playlistId
                )
                    ?: return@launch

            val tracks =
                if (playlist.trackIds.isEmpty()) {
                    emptyList()
                } else {
                    playlistInteractor
                        .getTracksByIds(
                            playlistId,
                            playlist.trackIds
                        )
                        .first()
                }

            updateState(
                playlist,
                tracks
            )
        }
    }

    fun deleteTrack(
        playlistId: Long,
        trackId: Long
    ) {
        viewModelScope.launch {

            playlistInteractor
                .deleteTrackFromPlaylist(
                    playlistId,
                    trackId
                )

            val playlist =
                playlistInteractor.getPlaylist(
                    playlistId
                )
                    ?: return@launch

            val tracks =
                if (playlist.trackIds.isEmpty()) {
                    emptyList()
                } else {
                    playlistInteractor
                        .getTracksByIds(
                            playlistId,
                            playlist.trackIds
                        )
                        .first()
                }

            updateState(
                playlist,
                tracks
            )
        }
    }

    fun deletePlaylist(
        playlistId: Long
    ) {
        viewModelScope.launch {

            playlistInteractor
                .deletePlaylist(
                    playlistId
                )

            _playlistDeleted.value =
                true
        }
    }

    private fun updateState(
        playlist: Playlist,
        tracks: List<Track>
    ) {
        val durationSum =
            tracks.sumOf {
                it.trackTime
            }

        val duration =
            (durationSum / 60000).toString()

        _state.value =
            PlaylistScreenState(
                playlist = playlist,
                tracks = tracks,
                tracksCount = tracks.size,
                duration = duration
            )
    }
}