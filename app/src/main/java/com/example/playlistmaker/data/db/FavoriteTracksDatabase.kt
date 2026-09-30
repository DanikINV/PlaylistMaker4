package com.example.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class
    ],
    version = 2
)
abstract class FavoriteTracksDatabase : RoomDatabase(){

    abstract fun favoriteTrackDao(): FavoriteTrackDao

    abstract fun playlistDao(): PlaylistDao
}