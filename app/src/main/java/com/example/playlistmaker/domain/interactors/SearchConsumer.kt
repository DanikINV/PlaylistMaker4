package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track

interface SearchConsumer {

    fun consume(tracks: List<Track>)

    fun consumeError()
}