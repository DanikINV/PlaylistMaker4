package com.example.playlistmaker.data.dto

import com.example.playlistmaker.domain.model.Track

data class TracksResponse(
    val resultCount: Int,
    val results: List<Track>
)