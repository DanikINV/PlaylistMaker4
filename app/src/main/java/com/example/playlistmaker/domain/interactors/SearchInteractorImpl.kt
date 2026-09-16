package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.repository.SearchRepository

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {

    override fun search(
        query: String,
        consumer: SearchConsumer
    ) {
        repository.search(query, consumer)
    }
}