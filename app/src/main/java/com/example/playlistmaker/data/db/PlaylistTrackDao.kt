package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {

    @Insert
    suspend fun insertTrack(
        track: PlaylistTrackEntity
    )

    @Delete
    suspend fun deleteTrack(
        track: PlaylistTrackEntity
    )

    @Query(
        "SELECT * FROM playlist_tracks " +
                "WHERE playlistId = :playlistId " +
                "ORDER BY rowid DESC"
    )
    fun getTracks(
        playlistId: Long
    ): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT * FROM playlist_tracks")
    fun getAllTracks(): Flow<List<PlaylistTrackEntity>>

    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM playlist_tracks " +
                "WHERE playlistId = :playlistId " +
                "AND trackId = :trackId" +
                ")"
    )
    suspend fun isTrackInPlaylist(
        playlistId: Long,
        trackId: Long
    ): Boolean

    @Query(
        "SELECT COUNT(*) FROM playlist_tracks " +
                "WHERE playlistId = :playlistId"
    )
    suspend fun getTrackCount(
        playlistId: Long
    ): Int

    @Query(
        "DELETE FROM playlist_tracks " +
                "WHERE playlistId = :playlistId " +
                "AND trackId = :trackId"
    )
    suspend fun deleteTrack(
        playlistId: Long,
        trackId: Long
    )

    @Query(
        "DELETE FROM playlist_tracks " +
                "WHERE playlistId = :playlistId"
    )
    suspend fun deleteTracksFromPlaylist(
        playlistId: Long
    )
}