package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(
        playlist: Playlist
    ) {
        repository.createPlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    override suspend fun addTrackToPlaylist(
        playlistId: Long,
        track: Track
    ): Boolean {
        return repository.addTrackToPlaylist(
            playlistId,
            track
        )
    }

    override suspend fun getPlaylist(
        playlistId: Long
    ): Playlist? {
        return repository.getPlaylist(playlistId)
    }

    override fun getPlaylistTracks(
        playlistId: Long
    ): Flow<List<Track>> {
        return repository.getPlaylistTracks(
            playlistId
        )
    }

    override fun getTracksByIds(
        playlistId: Long,
        trackIds: List<Long>
    ): Flow<List<Track>> {
        return repository.getTracksByIds(
            playlistId,
            trackIds
        )
    }

    override suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) {
        repository.deleteTrackFromPlaylist(
            playlistId,
            trackId
        )
    }

    override suspend fun deletePlaylist(
        playlistId: Long
    ) {
        repository.deletePlaylist(
            playlistId
        )
    }

    override suspend fun updatePlaylist(
        playlist: Playlist
    ) {
        repository.updatePlaylist(
            playlist
        )
    }
}