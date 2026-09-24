package com.example.playlistmaker.presentation.model

import com.example.playlistmaker.domain.model.Track

enum class PlayerPlaybackState {
    DEFAULT,
    PREPARED,
    PLAYING,
    PAUSED
}

data class PlayerScreenState(
    val track: Track,
    val playbackState: PlayerPlaybackState = PlayerPlaybackState.DEFAULT,
    val progress: Int = 0,
    val isPlayEnabled: Boolean = false
)