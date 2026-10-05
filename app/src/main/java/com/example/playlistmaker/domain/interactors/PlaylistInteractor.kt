package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {

    suspend fun createPlaylist(
        playlist: Playlist
    )

    suspend fun updatePlaylist(
        playlist: Playlist
    )

    fun getPlaylists(): Flow<List<Playlist>>

    suspend fun addTrackToPlaylist(
        playlistId: Long,
        track: Track
    ): Boolean

    suspend fun getPlaylist(
        playlistId: Long
    ): Playlist?

    fun getPlaylistTracks(
        playlistId: Long
    ): Flow<List<Track>>

    fun getTracksByIds(
        playlistId: Long,
        trackIds: List<Long>
    ): Flow<List<Track>>

    suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    )

    suspend fun deletePlaylist(
        playlistId: Long
    )
}