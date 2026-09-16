package com.example.playlistmaker.domain.model

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTime: Long,
    val artworkUrl100: String?,
    val collectionName: String? = null,
    val releaseDate: String? = null,
    val primaryGenreName: String? = null,
    val country: String? = null,
    val previewUrl: String? = null
) {

    fun getCoverArtwork(): String? {
        return artworkUrl100?.replaceAfterLast(
            '/',
            "512x512bb.jpg"
        )
    }
}