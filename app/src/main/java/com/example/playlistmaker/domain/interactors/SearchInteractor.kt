package com.example.playlistmaker.domain.interactors

interface SearchInteractor {

    fun search(
        query: String,
        consumer: SearchConsumer
    )
}