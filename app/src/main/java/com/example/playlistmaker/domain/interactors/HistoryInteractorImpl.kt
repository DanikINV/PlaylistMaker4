package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.HistoryRepository

class HistoryInteractorImpl(
    private val repository: HistoryRepository
) : HistoryInteractor {

    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun addTrack(track: Track) {

        val history = repository.getHistory().toMutableList()

        history.removeAll {
            it.trackId == track.trackId
        }

        history.add(0, track)

        while (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }

        repository.saveHistory(history)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
}