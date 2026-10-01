package com.example.playlistmaker.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithTracks(
    @Embedded
    val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "playlistId",
        entityColumn = "playlistId"
    )
    val tracks: List<PlaylistTrackEntity>
)