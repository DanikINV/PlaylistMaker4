package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.flow.Flow


interface PlaylistRepository {


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