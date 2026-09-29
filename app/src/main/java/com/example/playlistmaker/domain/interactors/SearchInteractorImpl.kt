package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {

    override fun search(
        query: String
    ): Flow<Result<List<Track>>> {
        return repository.search(query)
    }
}