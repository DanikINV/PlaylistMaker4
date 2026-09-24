package com.example.playlistmaker.presentation.model

import com.example.playlistmaker.domain.model.Track

enum class SearchContent {
    EMPTY,
    HISTORY,
    TRACKS,
    NOTHING_FOUND,
    ERROR
}

data class SearchScreenState(
    val tracks: List<Track> = emptyList(),
    val content: SearchContent = SearchContent.EMPTY,
    val isLoading: Boolean = false,
    val showClearButton: Boolean = false,
    val selectedTrack: Track? = null
)