package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.db.FavoriteTracksDatabase
import com.example.playlistmaker.data.db.PlaylistEntity
import com.example.playlistmaker.data.db.PlaylistTrackEntity
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val database: FavoriteTracksDatabase
) : PlaylistRepository {

    override suspend fun createPlaylist(
        playlist: Playlist
    ) {
        database
            .playlistDao()
            .insertPlaylist(
                PlaylistEntity(
                    playlistId = playlist.playlistId,
                    name = playlist.name,
                    description = playlist.description,
                    coverPath = playlist.coverPath,
                    tracksCount = playlist.tracksCount
                )
            )
    }

    override suspend fun updatePlaylist(
        playlist: Playlist
    ) {
        database
            .playlistDao()
            .updatePlaylist(
                PlaylistEntity(
                    playlistId = playlist.playlistId,
                    name = playlist.name,
                    description = playlist.description,
                    coverPath = playlist.coverPath,
                    tracksCount = playlist.tracksCount
                )
            )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return database
            .playlistDao()
            .getPlaylists()
            .map { entities ->

                entities.map { entity ->

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

        val dao =
            database.playlistTrackDao()

        if (
            dao.isTrackInPlaylist(
                playlistId,
                track.trackId
            )
        ) {
            return false
        }

        dao.insertTrack(
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

        val playlist =
            database
                .playlistDao()
                .getPlaylist(
                    playlistId
                )
                ?: return true

        val trackCount =
            dao.getTrackCount(
                playlistId
            )

        database
            .playlistDao()
            .updatePlaylist(
                playlist.copy(
                    tracksCount = trackCount
                )
            )

        return true
    }

    override suspend fun getPlaylist(
        playlistId: Long
    ): Playlist? {

        val entity =
            database
                .playlistDao()
                .getPlaylist(
                    playlistId
                )
                ?: return null

        val trackIds =
            database
                .playlistTrackDao()
                .getTracks(
                    playlistId
                )
                .first()
                .map {
                    it.trackId
                }

        return Playlist(
            playlistId = entity.playlistId,
            name = entity.name,
            description = entity.description,
            coverPath = entity.coverPath,
            trackIds = trackIds,
            tracksCount = entity.tracksCount
        )
    }

    override fun getPlaylistTracks(
        playlistId: Long
    ): Flow<List<Track>> {

        return database
            .playlistTrackDao()
            .getTracks(
                playlistId
            )
            .map { entities ->

                entities.map { entity ->
                    entity.toTrack()
                }
            }
    }

    override fun getTracksByIds(
        playlistId: Long,
        trackIds: List<Long>
    ): Flow<List<Track>> {

        return database
            .playlistTrackDao()
            .getTracks(
                playlistId
            )
            .map { entities ->

                entities
                    .filter {
                        it.trackId in trackIds
                    }
                    .map {
                        it.toTrack()
                    }
            }
    }

    override suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) {

        val playlistTrackDao =
            database.playlistTrackDao()

        val playlistDao =
            database.playlistDao()

        playlistTrackDao.deleteTrack(
            playlistId,
            trackId
        )

        val isTrackUsedInOtherPlaylists =
            playlistDao.isTrackUsed(
                trackId
            )

        val playlist =
            playlistDao.getPlaylist(
                playlistId
            )
                ?: return

        val trackCount =
            playlistTrackDao.getTrackCount(
                playlistId
            )

        playlistDao.updatePlaylist(
            playlist.copy(
                tracksCount = trackCount
            )
        )

        if (!isTrackUsedInOtherPlaylists) {
        }
    }

    override suspend fun deletePlaylist(
        playlistId: Long
    ) {

        database
            .playlistTrackDao()
            .deleteTracksFromPlaylist(
                playlistId
            )

        database
            .playlistDao()
            .deletePlaylist(
                playlistId
            )
    }

    private fun PlaylistTrackEntity.toTrack(): Track {

        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTime = trackTime,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl,
            isFavorite = false
        )
    }
}