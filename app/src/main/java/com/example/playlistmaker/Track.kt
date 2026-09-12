package com.example.playlistmaker

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    @SerializedName("trackId")
    val trackId: Long,
    @SerializedName("trackName")
    val trackName: String,
    @SerializedName("artistName")
    val artistName: String,
    @SerializedName("trackTimeMillis")
    val trackTime: Long,
    @SerializedName("artworkUrl100")
    val artworkUrl100: String?,
    @SerializedName("collectionName")
    val collectionName: String? = null,
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    @SerializedName("primaryGenreName")
    val primaryGenreName: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("previewUrl")
    val previewUrl: String? = null
) : Parcelable {
    fun getCoverArtwork(): String? =
        artworkUrl100?.replaceAfterLast('/', "512x512bb.jpg")
}