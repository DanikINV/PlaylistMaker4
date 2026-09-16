package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.interactors.SearchConsumer

interface SearchRepository {

    fun search(
        query: String,
        consumer: SearchConsumer
    )
}