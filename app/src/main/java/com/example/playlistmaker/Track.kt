package com.example.playlistmaker

data class Track(
    val trackName: String,
    val artistName: String,
    val trackTime: Long,
    val artworkUrl100: String?
)