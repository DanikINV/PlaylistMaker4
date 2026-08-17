package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val prefs: SharedPreferences) {

    fun getHistory(): List<Track> {
        val json = prefs.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Track>>() {}.type
            Gson().fromJson<List<Track>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            while (history.size > MAX_HISTORY_SIZE) {
                history.removeAt(history.size - 1)
            }
        }
        saveHistory(history)
    }

    fun clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }

    private fun saveHistory(history: List<Track>) {
        prefs.edit().putString(HISTORY_KEY, Gson().toJson(history)).apply()
    }

    companion object {
        private const val HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }
}