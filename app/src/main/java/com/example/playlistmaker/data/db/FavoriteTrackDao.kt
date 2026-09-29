package com.example.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTrackDao {

    @Insert
    suspend fun insertTrack(track: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Long)

    @Query("""
        SELECT *
        FROM favorite_tracks
        ORDER BY addedAt DESC
    """)
    fun getFavoriteTracks(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<Long>

    @Query("""
        SELECT EXISTS(
            SELECT 1
            FROM favorite_tracks
            WHERE trackId = :trackId
        )
    """)
    suspend fun isFavorite(trackId: Long): Boolean
}