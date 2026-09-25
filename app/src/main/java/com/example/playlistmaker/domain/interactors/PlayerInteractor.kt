package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track

interface PlayerInteractor {
    fun getTrack(): Track
}