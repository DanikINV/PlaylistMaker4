package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.toDomain
import com.example.playlistmaker.data.dto.toDto
import com.example.playlistmaker.data.storage.SearchHistoryStorage
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val storage: SearchHistoryStorage
) : HistoryRepository {

    override fun getHistory(): List<Track> {
        return storage.getHistory().map {
            it.toDomain()
        }
    }

    override fun saveHistory(history: List<Track>) {

        val dtoHistory = history.map {
            it.toDto()
        }

        storage.saveHistory(dtoHistory)
    }

    override fun clearHistory() {
        storage.clearHistory()
    }
}