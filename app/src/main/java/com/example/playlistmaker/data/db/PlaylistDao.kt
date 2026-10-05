package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.playlistmaker.data.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert
    suspend fun insertPlaylist(
        playlist: PlaylistEntity
    )

    @Update
    suspend fun updatePlaylist(
        playlist: PlaylistEntity
    )

    @Query("SELECT * FROM playlists")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Query(
        "SELECT * FROM playlists " +
                "WHERE playlistId = :playlistId"
    )
    suspend fun getPlaylist(
        playlistId: Long
    ): PlaylistEntity?

    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM playlist_tracks " +
                "WHERE trackId = :trackId" +
                ")"
    )
    suspend fun isTrackUsed(
        trackId: Long
    ): Boolean

    @Query(
        "DELETE FROM playlists " +
                "WHERE playlistId = :playlistId"
    )
    suspend fun deletePlaylist(
        playlistId: Long
    )
}