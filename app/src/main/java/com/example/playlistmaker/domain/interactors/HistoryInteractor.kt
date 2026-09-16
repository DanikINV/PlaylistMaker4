package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track

interface HistoryInteractor {

    fun getHistory(): List<Track>

    fun addTrack(track: Track)

    fun clearHistory()
}