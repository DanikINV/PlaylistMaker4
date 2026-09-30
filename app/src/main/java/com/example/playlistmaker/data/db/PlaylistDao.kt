package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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



    @Query(
        "SELECT * FROM playlists"
    )
    fun getPlaylists():
            Flow<List<PlaylistEntity>>



    @Query(
        "SELECT * FROM playlists WHERE playlistId = :playlistId"
    )
    suspend fun getPlaylist(
        playlistId: Long
    ): PlaylistEntity?
}