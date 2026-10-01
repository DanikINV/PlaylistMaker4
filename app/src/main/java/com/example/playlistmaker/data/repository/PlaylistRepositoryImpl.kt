package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.PlaylistDao
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistTrackDao
import com.example.playlistmaker.data.db.PlaylistTrackEntity
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao
) : PlaylistRepository {

    override suspend fun createPlaylist(
        playlist: Playlist
    ) {
        playlistDao.insertPlaylist(
            PlaylistEntity(
                name = playlist.name,
                description = playlist.description,
                coverPath = playlist.coverPath,
                tracksCount = 0
            )
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists().map { list ->
            list.map { entity ->
                Playlist(
                    playlistId = entity.playlistId,
                    name = entity.name,
                    description = entity.description,
                    coverPath = entity.coverPath,
                    trackIds = emptyList(),
                    tracksCount = entity.tracksCount
                )
            }
        }
    }

    override suspend fun addTrackToPlaylist(
        playlistId: Long,
        track: Track
    ): Boolean {

        val playlist = playlistDao.getPlaylist(playlistId)
            ?: return false

        if (
            playlistTrackDao.isTrackInPlaylist(
                playlistId = playlistId,
                trackId = track.trackId
            )
        ) {
            return false
        }

        playlistTrackDao.insertTrack(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTime = track.trackTime,
                artworkUrl100 = track.artworkUrl100,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                previewUrl = track.previewUrl
            )
        )

        val tracksCount = playlistTrackDao.getTrackCount(
            playlistId
        )

        playlistDao.updatePlaylist(
            playlist.copy(
                tracksCount = tracksCount
            )
        )

        return true
    }
}