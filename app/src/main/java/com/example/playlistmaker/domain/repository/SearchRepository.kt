package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun search(
        query: String
    ): Flow<Result<List<Track>>>
}