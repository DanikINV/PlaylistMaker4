package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.FavoriteTrackRepository
import kotlinx.coroutines.flow.Flow

class FavoriteTrackInteractorImpl(
    private val repository: FavoriteTrackRepository
) : FavoriteTrackInteractor {

    override suspend fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override suspend fun deleteTrack(track: Track) {
        repository.deleteTrack(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return repository.getFavoriteTracks()
    }
}