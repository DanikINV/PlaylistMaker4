package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.flow.Flow


interface PlaylistInteractor {


    suspend fun createPlaylist(
        playlist: Playlist
    )



    fun getPlaylists():
            Flow<List<Playlist>>



    suspend fun addTrackToPlaylist(
        playlistId: Long,
        trackId: Long
    ): Boolean
}