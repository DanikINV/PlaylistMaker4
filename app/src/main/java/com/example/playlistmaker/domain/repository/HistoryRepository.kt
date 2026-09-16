package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface HistoryRepository {

    fun getHistory(): List<Track>

    fun saveHistory(history: List<Track>)

    fun clearHistory()
}