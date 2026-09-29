package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTrackDao
import com.example.playlistmaker.data.db.FavoriteTrackEntity
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.FavoriteTrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTrackRepositoryImpl(
    private val favoriteTrackDao: FavoriteTrackDao
) : FavoriteTrackRepository {

    override suspend fun addTrack(track: Track) {
        favoriteTrackDao.insertTrack(
            FavoriteTrackEntity(
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTime = track.trackTime,
                artworkUrl100 = track.artworkUrl100,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                previewUrl = track.previewUrl,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteTrack(track: Track) {
        favoriteTrackDao.deleteTrack(track.trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoriteTrackDao
            .getFavoriteTracks()
            .map { entities ->
                entities.map { entity ->
                    Track(
                        trackId = entity.trackId,
                        trackName = entity.trackName,
                        artistName = entity.artistName,
                        trackTime = entity.trackTime,
                        artworkUrl100 = entity.artworkUrl100,
                        collectionName = entity.collectionName,
                        releaseDate = entity.releaseDate,
                        primaryGenreName = entity.primaryGenreName,
                        country = entity.country,
                        previewUrl = entity.previewUrl,
                        isFavorite = true
                    )
                }
            }
    }
}