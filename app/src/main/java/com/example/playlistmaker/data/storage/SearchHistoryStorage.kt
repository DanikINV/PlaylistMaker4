package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryStorage(
    private val prefs: SharedPreferences
) {

    private val gson = Gson()

    fun getHistory(): List<TrackDto> {

        val json = prefs.getString(
            HISTORY_KEY,
            null
        ) ?: return emptyList()

        return try {

            val type = object : TypeToken<List<TrackDto>>() {}.type

            gson.fromJson<List<TrackDto>>(
                json,
                type
            ) ?: emptyList()

        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHistory(history: List<TrackDto>) {

        prefs.edit()
            .putString(
                HISTORY_KEY,
                gson.toJson(history)
            )
            .apply()
    }

    fun clearHistory() {

        prefs.edit()
            .remove(HISTORY_KEY)
            .apply()
    }

    companion object {
        private const val HISTORY_KEY = "search_history"
    }
}